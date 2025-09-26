package ejb;

import entity.Suppliers;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class SupplierEJB {
    
    private static final Logger logger = Logger.getLogger(SupplierEJB.class.getName());
    
    @PersistenceContext(unitName = "semiconductor-pu")
    private EntityManager em;
    
    // =============== CRUD OPERATIONS ===============
    
    /**
     * Create a new supplier
     */
    public void create(Suppliers supplier) {
        try {
            em.persist(supplier);
            em.flush();
            
            logger.info("✅ Created new supplier: " + supplier.getName() + 
                       " with ID: " + supplier.getSupplierId());
                       
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error creating supplier: " + supplier.getName(), e);
            throw new RuntimeException("Failed to create supplier: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find supplier by ID
     */
    public Suppliers findById(Integer id) {
        try {
            Suppliers supplier = em.find(Suppliers.class, id);
            if (supplier == null) {
                logger.warning("⚠️ Supplier not found with ID: " + id);
            }
            return supplier;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching supplier with ID: " + id, e);
            throw new RuntimeException("Failed to fetch supplier: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all active suppliers
     */
    public List<Suppliers> findAll() {
        try {
            TypedQuery<Suppliers> query = em.createNamedQuery("Suppliers.findAll", Suppliers.class);
            List<Suppliers> suppliers = query.getResultList();
            
            logger.info("🏢 Retrieved " + suppliers.size() + " suppliers from database");
            return suppliers;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching all suppliers", e);
            throw new RuntimeException("Failed to fetch suppliers: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update supplier information
     */
    public void update(Suppliers supplier) {
        try {
            Suppliers existingSupplier = em.find(Suppliers.class, supplier.getSupplierId());
            if (existingSupplier == null) {
                throw new RuntimeException("Supplier not found for update: " + supplier.getSupplierId());
            }
            
            // Update supplier fields
            existingSupplier.setName(supplier.getName());
            existingSupplier.setEmail(supplier.getEmail());
            existingSupplier.setPhone(supplier.getPhone());
            existingSupplier.setAddress(supplier.getAddress());
            existingSupplier.setContactPerson(supplier.getContactPerson());
            existingSupplier.setCountry(supplier.getCountry());
            existingSupplier.setWebsite(supplier.getWebsite());
            existingSupplier.setRating(supplier.getRating());
            existingSupplier.setNotes(supplier.getNotes());
            
            em.merge(existingSupplier);
            em.flush();
            
            logger.info("✅ Updated supplier: " + supplier.getName());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error updating supplier: " + supplier.getSupplierId(), e);
            throw new RuntimeException("Failed to update supplier: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete supplier (soft delete)
     */
    public void delete(Integer id) {
        try {
            Suppliers supplier = em.find(Suppliers.class, id);
            if (supplier != null) {
                supplier.setActive(false);
                em.merge(supplier);
                em.flush();
                
                logger.info("🗑️ Soft deleted supplier: " + supplier.getName() + " (ID: " + id + ")");
            } else {
                logger.warning("⚠️ Attempted to delete non-existent supplier with ID: " + id);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error deleting supplier with ID: " + id, e);
            throw new RuntimeException("Failed to delete supplier: " + e.getMessage(), e);
        }
    }
    
    // =============== QUERY METHODS ===============
    
    /**
     * Find suppliers by country
     */
    public List<Suppliers> findByCountry(String country) {
        try {
            TypedQuery<Suppliers> query = em.createNamedQuery("Suppliers.findByCountry", Suppliers.class);
            query.setParameter("country", country);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching suppliers by country: " + country, e);
            throw new RuntimeException("Failed to fetch suppliers by country: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search suppliers by name or email
     */
    public List<Suppliers> searchSuppliers(String searchTerm) {
        try {
            TypedQuery<Suppliers> query = em.createQuery(
                "SELECT s FROM Suppliers s WHERE " +
                "(LOWER(s.name) LIKE LOWER(:searchTerm) OR " +
                "LOWER(s.email) LIKE LOWER(:searchTerm) OR " +
                "LOWER(s.contactPerson) LIKE LOWER(:searchTerm)) " +
                "AND s.active = true ORDER BY s.name", 
                Suppliers.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error searching suppliers with term: " + searchTerm, e);
            throw new RuntimeException("Failed to search suppliers: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get top-rated suppliers
     */
    public List<Suppliers> getTopRatedSuppliers(int limit) {
        try {
            TypedQuery<Suppliers> query = em.createQuery(
                "SELECT s FROM Suppliers s WHERE s.active = true ORDER BY s.rating DESC, s.name", 
                Suppliers.class);
            query.setMaxResults(limit);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching top-rated suppliers", e);
            throw new RuntimeException("Failed to fetch top-rated suppliers: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get total number of active suppliers
     */
    public long getTotalSuppliers() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(s) FROM Suppliers s WHERE s.active = true", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error counting total suppliers", e);
            return 0L;
        }
    }
    
    /**
     * Find suppliers by component ID
     * This is a placeholder method since there's no direct relationship
     * In a real system, you'd have a supplier-component relationship table
     */
    public List<Suppliers> findSuppliersByComponent(Integer componentId) {
        try {
            // For now, return all active suppliers as a placeholder
            // In a real implementation, you'd join with a supplier_components table
            TypedQuery<Suppliers> query = em.createQuery(
                "SELECT s FROM Suppliers s WHERE s.active = true ORDER BY s.rating DESC", 
                Suppliers.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error finding suppliers for component ID: " + componentId, e);
            throw new RuntimeException("Failed to find suppliers for component: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create new supplier
     */
    public void createSupplier(Suppliers supplier) {
        try {
            em.persist(supplier);
            logger.info("✅ Supplier created: " + supplier.getName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error creating supplier: " + supplier.getName(), e);
            throw new RuntimeException("Failed to create supplier: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update supplier
     */
    public void updateSupplier(Suppliers supplier) {
        try {
            em.merge(supplier);
            logger.info("📝 Supplier updated: " + supplier.getName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error updating supplier: " + supplier.getName(), e);
            throw new RuntimeException("Failed to update supplier: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete supplier
     */
    public void deleteSupplier(Integer supplierId) {
        try {
            Suppliers supplier = em.find(Suppliers.class, supplierId);
            if (supplier != null) {
                em.remove(supplier);
                logger.info("🗑️ Supplier deleted: " + supplier.getName());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error deleting supplier ID: " + supplierId, e);
            throw new RuntimeException("Failed to delete supplier: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get suppliers by country
     */
    public List<Suppliers> getSuppliersByCountry(String country) {
        try {
            TypedQuery<Suppliers> query = em.createQuery(
                "SELECT s FROM Suppliers s WHERE s.country = :country ORDER BY s.name", 
                Suppliers.class);
            query.setParameter("country", country);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error finding suppliers by country: " + country, e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Get all suppliers
     */
    public List<Suppliers> getAllSuppliers() {
        try {
            TypedQuery<Suppliers> query = em.createQuery(
                "SELECT s FROM Suppliers s ORDER BY s.name", 
                Suppliers.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error finding all suppliers", e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Find active suppliers
     */
    public List<Suppliers> findActiveSuppliers() {
        try {
            TypedQuery<Suppliers> query = em.createQuery(
                "SELECT s FROM Suppliers s WHERE s.active = true ORDER BY s.name", 
                Suppliers.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error finding active suppliers", e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Check if supplier email is unique
     */
    public boolean isSupplierEmailUnique(String email, Integer excludeId) {
        try {
            String jpql = "SELECT COUNT(s) FROM Suppliers s WHERE s.email = :email";
            if (excludeId != null) {
                jpql += " AND s.supplierId != :excludeId";
            }
            
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("email", email);
            if (excludeId != null) {
                query.setParameter("excludeId", excludeId);
            }
            
            return query.getSingleResult() == 0;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error checking supplier email uniqueness", e);
            return false;
        }
    }
}