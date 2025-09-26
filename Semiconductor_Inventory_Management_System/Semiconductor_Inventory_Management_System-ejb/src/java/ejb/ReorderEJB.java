/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ejb;

import entity.Components;
import entity.ReorderRequests;
import entity.Suppliers;
import entity.Users;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReorderEJB - Business logic for managing reorder requests
 * Handles automatic and manual reorder request creation and processing
 * 
 * @author MINH_QUAN - Created on 2025-09-16
 */
@Stateless
public class ReorderEJB {
    
    private static final Logger logger = Logger.getLogger(ReorderEJB.class.getName());
    
    @PersistenceContext(unitName = "semiconductor-pu")
    private EntityManager em;
    
    @EJB
    private ComponentEJB componentEJB;
    
    @EJB
    private SupplierEJB supplierEJB;
    
    @EJB
    private NotificationService notificationService;
    
    /**
     * Create a new reorder request
     */
    public ReorderRequests createReorderRequest(ReorderRequests request) {
        try {
            request.setRequestDate(new Date());
            request.setStatus("PENDING");
            
            // Calculate estimated cost if not provided
            if (request.getEstimatedCost() == null && request.getComponentId() != null) {
                BigDecimal unitPrice = request.getComponentId().getUnitPrice();
                if (unitPrice != null) {
                    request.setEstimatedCost(unitPrice.multiply(new BigDecimal(request.getRequestedQuantity())));
                }
            }
            
            em.persist(request);
            em.flush();
            
            logger.log(Level.INFO, "Created reorder request with ID: {0}", request.getRequestId());
            
            // Send notification
            notificationService.sendReorderNotification(request);
            
            return request;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating reorder request", e);
            throw new RuntimeException("Failed to create reorder request: " + e.getMessage());
        }
    }
    
    /**
     * Update an existing reorder request
     */
    public ReorderRequests updateReorderRequest(ReorderRequests request) {
        try {
            ReorderRequests existing = em.find(ReorderRequests.class, request.getRequestId());
            if (existing == null) {
                throw new IllegalArgumentException("Reorder request not found");
            }
            
            // Update allowed fields
            existing.setRequestedQuantity(request.getRequestedQuantity());
            existing.setEstimatedCost(request.getEstimatedCost());
            existing.setPriority(request.getPriority());
            existing.setReason(request.getReason());
            existing.setExpectedDelivery(request.getExpectedDelivery());
            existing.setNotes(request.getNotes());
            
            em.merge(existing);
            return existing;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating reorder request", e);
            throw new RuntimeException("Failed to update reorder request: " + e.getMessage());
        }
    }
    
    /**
     * Approve a reorder request
     */
    public void approveReorderRequest(Integer requestId, Integer approvedBy) {
        try {
            ReorderRequests request = em.find(ReorderRequests.class, requestId);
            if (request == null) {
                throw new IllegalArgumentException("Reorder request not found");
            }
            
            Users approver = em.find(Users.class, approvedBy);
            if (approver == null) {
                throw new IllegalArgumentException("Approver not found");
            }
            
            request.setStatus("APPROVED");
            request.setApprovedBy(approver);
            request.setApprovedAt(new Date());
            
            em.merge(request);
            
            logger.log(Level.INFO, "Approved reorder request {0} by user {1}", 
                    new Object[]{requestId, approver.getUsername()});
            
            // Send approval notification
            notificationService.sendReorderApprovalNotification(request);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error approving reorder request", e);
            throw new RuntimeException("Failed to approve reorder request: " + e.getMessage());
        }
    }
    
    /**
     * Reject a reorder request
     */
    public void rejectReorderRequest(Integer requestId, String reason) {
        try {
            ReorderRequests request = em.find(ReorderRequests.class, requestId);
            if (request == null) {
                throw new IllegalArgumentException("Reorder request not found");
            }
            
            request.setStatus("REJECTED");
            request.setNotes(request.getNotes() + "\nRejection reason: " + reason);
            
            em.merge(request);
            
            logger.log(Level.INFO, "Rejected reorder request {0}", requestId);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error rejecting reorder request", e);
            throw new RuntimeException("Failed to reject reorder request: " + e.getMessage());
        }
    }
    
    /**
     * Mark reorder request as ordered
     */
    public void markAsOrdered(Integer requestId) {
        try {
            ReorderRequests request = em.find(ReorderRequests.class, requestId);
            if (request == null) {
                throw new IllegalArgumentException("Reorder request not found");
            }
            
            request.setStatus("ORDERED");
            request.setOrderDate(new Date());
            
            em.merge(request);
            
            logger.log(Level.INFO, "Marked reorder request {0} as ordered", requestId);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error marking reorder request as ordered", e);
            throw new RuntimeException("Failed to mark reorder request as ordered: " + e.getMessage());
        }
    }
    
    /**
     * Complete a reorder request (when stock arrives)
     */
    public void completeReorderRequest(Integer requestId, Integer actualQuantity) {
        try {
            ReorderRequests request = em.find(ReorderRequests.class, requestId);
            if (request == null) {
                throw new IllegalArgumentException("Reorder request not found");
            }
            
            request.setStatus("COMPLETED");
            
            // Update component stock
            Components component = request.getComponentId();
            if (component != null) {
                component.setQuantity(component.getQuantity() + actualQuantity);
                component.setUpdatedAt(new Date());
                em.merge(component);
            }
            
            em.merge(request);
            
            logger.log(Level.INFO, "Completed reorder request {0} with quantity {1}", 
                    new Object[]{requestId, actualQuantity});
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error completing reorder request", e);
            throw new RuntimeException("Failed to complete reorder request: " + e.getMessage());
        }
    }
    
    /**
     * Find all reorder requests
     */
    public List<ReorderRequests> findAllReorderRequests() {
        try {
            TypedQuery<ReorderRequests> query = em.createNamedQuery("ReorderRequests.findAll", ReorderRequests.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding reorder requests", e);
            throw new RuntimeException("Failed to find reorder requests: " + e.getMessage());
        }
    }
    
    /**
     * Find reorder requests by status
     */
    public List<ReorderRequests> findReorderRequestsByStatus(String status) {
        try {
            TypedQuery<ReorderRequests> query = em.createNamedQuery("ReorderRequests.findByStatus", ReorderRequests.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding reorder requests by status", e);
            throw new RuntimeException("Failed to find reorder requests by status: " + e.getMessage());
        }
    }
    
    /**
     * Find pending reorder requests
     */
    public List<ReorderRequests> findPendingReorderRequests() {
        return findReorderRequestsByStatus("PENDING");
    }
    
    /**
     * Find approved reorder requests
     */
    public List<ReorderRequests> findApprovedReorderRequests() {
        return findReorderRequestsByStatus("APPROVED");
    }
    
    /**
     * Find reorder request by ID
     */
    public ReorderRequests findReorderRequestById(Integer requestId) {
        try {
            return em.find(ReorderRequests.class, requestId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error finding reorder request by ID", e);
            throw new RuntimeException("Failed to find reorder request: " + e.getMessage());
        }
    }
    
    /**
     * Create automatic reorder requests for components below reorder level
     */
    public void createAutomaticReorderRequests() {
        try {
            List<Components> lowStockComponents = componentEJB.findComponentsBelowReorderLevel();
            
            for (Components component : lowStockComponents) {
                // Check if there's already a pending request for this component
                TypedQuery<ReorderRequests> query = em.createQuery(
                    "SELECT r FROM ReorderRequests r WHERE r.componentId = :component AND r.status IN ('PENDING', 'APPROVED', 'ORDERED')", 
                    ReorderRequests.class);
                query.setParameter("component", component);
                
                if (query.getResultList().isEmpty()) {
                    // Create automatic reorder request
                    ReorderRequests autoRequest = new ReorderRequests();
                    autoRequest.setComponentId(component);
                    autoRequest.setRequestedQuantity(component.getReorderLevel() * 2); // Order double the reorder level
                    autoRequest.setPriority("MEDIUM");
                    autoRequest.setReason("Automatic reorder - stock below minimum level");
                    autoRequest.setAutoGenerated(true);
                    autoRequest.setStatus("PENDING");
                    autoRequest.setRequestDate(new Date());
                    
                    // Find preferred supplier
                    List<Suppliers> suppliers = supplierEJB.findSuppliersByComponent(component.getComponentId());
                    if (!suppliers.isEmpty()) {
                        autoRequest.setSupplierId(suppliers.get(0)); // Use first available supplier
                    }
                    
                    createReorderRequest(autoRequest);
                    
                    logger.log(Level.INFO, "Created automatic reorder request for component: {0}", 
                            component.getName());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating automatic reorder requests", e);
            throw new RuntimeException("Failed to create automatic reorder requests: " + e.getMessage());
        }
    }
    
    /**
     * Get reorder request statistics
     */
    public ReorderStatistics getReorderStatistics() {
        try {
            Long totalRequests = em.createQuery("SELECT COUNT(r) FROM ReorderRequests r", Long.class)
                    .getSingleResult();
            
            Long pendingRequests = em.createQuery("SELECT COUNT(r) FROM ReorderRequests r WHERE r.status = 'PENDING'", Long.class)
                    .getSingleResult();
            
            Long approvedRequests = em.createQuery("SELECT COUNT(r) FROM ReorderRequests r WHERE r.status = 'APPROVED'", Long.class)
                    .getSingleResult();
            
            Long completedRequests = em.createQuery("SELECT COUNT(r) FROM ReorderRequests r WHERE r.status = 'COMPLETED'", Long.class)
                    .getSingleResult();
            
            BigDecimal totalEstimatedCost = em.createQuery("SELECT COALESCE(SUM(r.estimatedCost), 0) FROM ReorderRequests r WHERE r.status IN ('PENDING', 'APPROVED')", BigDecimal.class)
                    .getSingleResult();
            
            return new ReorderStatistics(totalRequests, pendingRequests, approvedRequests, completedRequests, totalEstimatedCost);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting reorder statistics", e);
            throw new RuntimeException("Failed to get reorder statistics: " + e.getMessage());
        }
    }
    
    /**
     * Inner class for reorder statistics
     */
    public static class ReorderStatistics {
        private final Long totalRequests;
        private final Long pendingRequests;
        private final Long approvedRequests;
        private final Long completedRequests;
        private final BigDecimal totalEstimatedCost;
        
        public ReorderStatistics(Long totalRequests, Long pendingRequests, Long approvedRequests, 
                Long completedRequests, BigDecimal totalEstimatedCost) {
            this.totalRequests = totalRequests;
            this.pendingRequests = pendingRequests;
            this.approvedRequests = approvedRequests;
            this.completedRequests = completedRequests;
            this.totalEstimatedCost = totalEstimatedCost;
        }
        
        // Getters
        public Long getTotalRequests() { return totalRequests; }
        public Long getPendingRequests() { return pendingRequests; }
        public Long getApprovedRequests() { return approvedRequests; }
        public Long getCompletedRequests() { return completedRequests; }
        public BigDecimal getTotalEstimatedCost() { return totalEstimatedCost; }
    }
    
    /**
     * Delete a reorder request (only if pending)
     */
    public void deleteReorderRequest(Integer requestId) {
        try {
            ReorderRequests request = em.find(ReorderRequests.class, requestId);
            if (request == null) {
                throw new IllegalArgumentException("Reorder request not found");
            }
            
            if (!"PENDING".equals(request.getStatus())) {
                throw new IllegalStateException("Can only delete pending reorder requests");
            }
            
            em.remove(request);
            
            logger.log(Level.INFO, "Deleted reorder request with ID: {0}", requestId);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting reorder request", e);
            throw new RuntimeException("Failed to delete reorder request: " + e.getMessage());
        }
    }
}