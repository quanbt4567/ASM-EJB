package ejb;

import entity.Components;
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

@Stateless
public class ComponentEJB {
    
    private static final Logger logger = Logger.getLogger(ComponentEJB.class.getName());
    
    @PersistenceContext(unitName = "semiconductor-pu")
    private EntityManager em;
    
    @EJB
    private NotificationService notificationService;
    
    // =============== CRUD OPERATIONS ===============
    
    /**
     * Create a new component
     */
    public void createComponent(Components component) {
        try {
            // Set timestamps
            Date now = new Date();
            component.setCreatedAt(now);
            component.setUpdatedAt(now);
            
            // Set default created_by if not set
            if (component.getCreatedBy() == null) {
                Users defaultUser = em.find(Users.class, 1);
                component.setCreatedBy(defaultUser);
            }
            
            // Set default values
            if (component.getCurrency() == null) {
                component.setCurrency("USD");
            }
            
            em.persist(component);
            em.flush();
            
            logger.info("✅ Created new component: " + component.getName() + 
                       " with ID: " + component.getComponentId());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error creating component: " + component.getName(), e);
            throw new RuntimeException("Failed to create component: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get component by ID
     */
    public Components getComponentById(Integer id) {
        try {
            Components component = em.find(Components.class, id);
            if (component == null) {
                logger.warning("⚠️ Component not found with ID: " + id);
            }
            return component;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching component with ID: " + id, e);
            throw new RuntimeException("Failed to fetch component: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all active components
     */
    public List<Components> getAllComponents() {
        try {
            TypedQuery<Components> query = em.createQuery(
                "SELECT c FROM Components c WHERE c.active = true ORDER BY c.name", 
                Components.class);
            List<Components> components = query.getResultList();
            
            logger.info("📦 Retrieved " + components.size() + " components from database");
            return components;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching all components", e);
            throw new RuntimeException("Failed to fetch components: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update component with stock level monitoring
     */
    public void updateComponent(Components component) {
        try {
            Components existingComponent = em.find(Components.class, component.getComponentId());
            if (existingComponent == null) {
                throw new RuntimeException("Component not found for update: " + component.getComponentId());
            }
            
            int oldQuantity = existingComponent.getQuantity();
            
            // Update component fields
            existingComponent.setName(component.getName());
            existingComponent.setCategory(component.getCategory());
            existingComponent.setDescription(component.getDescription());
            existingComponent.setPartNumber(component.getPartNumber());
            existingComponent.setManufacturer(component.getManufacturer());
            existingComponent.setQuantity(component.getQuantity());
            existingComponent.setReorderLevel(component.getReorderLevel());
            existingComponent.setUnitPrice(component.getUnitPrice());
            existingComponent.setCurrency(component.getCurrency());
            existingComponent.setLocation(component.getLocation());
            existingComponent.setDatasheetUrl(component.getDatasheetUrl());
            existingComponent.setUpdatedAt(new Date());
            
            em.merge(existingComponent);
            em.flush();
            
            // Check stock levels and trigger alerts if needed
            checkStockLevelsAndAlert(existingComponent, oldQuantity);
            
            logger.info("✅ Updated component: " + component.getName() + 
                       " (Quantity: " + oldQuantity + " → " + component.getQuantity() + ")");
                       
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error updating component: " + component.getComponentId(), e);
            throw new RuntimeException("Failed to update component: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete component (soft delete)
     */
    public void deleteComponent(Integer id) {
        try {
            Components component = em.find(Components.class, id);
            if (component != null) {
                component.setActive(false);
                component.setUpdatedAt(new Date());
                em.merge(component);
                em.flush();
                
                logger.info("🗑️ Soft deleted component: " + component.getName() + " (ID: " + id + ")");
            } else {
                logger.warning("⚠️ Attempted to delete non-existent component with ID: " + id);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error deleting component with ID: " + id, e);
            throw new RuntimeException("Failed to delete component: " + e.getMessage(), e);
        }
    }
    
    // =============== STOCK MANAGEMENT ===============
    
    /**
     * Update stock quantity and trigger alerts if necessary
     */
    public void updateStock(Integer componentId, int newQuantity) {
        try {
            Components component = getComponentById(componentId);
            if (component == null) {
                throw new RuntimeException("Component not found for stock update: " + componentId);
            }
            
            int oldQuantity = component.getQuantity();
            component.setQuantity(newQuantity);
            component.setUpdatedAt(new Date());
            
            em.merge(component);
            em.flush();
            
            checkStockLevelsAndAlert(component, oldQuantity);
            
            logger.info("📊 Stock updated for " + component.getName() + 
                       ": " + oldQuantity + " → " + newQuantity);
                       
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error updating stock for component: " + componentId, e);
            throw new RuntimeException("Failed to update stock: " + e.getMessage(), e);
        }
    }
    
    /**
     * Consume stock (for transactions)
     */
    public void consumeStock(Integer componentId, int quantity) {
        try {
            Components component = getComponentById(componentId);
            if (component == null) {
                throw new RuntimeException("Component not found: " + componentId);
            }
            
            int oldQuantity = component.getQuantity();
            int newQuantity = Math.max(0, oldQuantity - quantity);
            
            component.setQuantity(newQuantity);
            component.setUpdatedAt(new Date());
            
            em.merge(component);
            em.flush();
            
            checkStockLevelsAndAlert(component, oldQuantity);
            
            logger.info("📉 Stock consumed for " + component.getName() + 
                       ": " + oldQuantity + " → " + newQuantity + " (consumed: " + quantity + ")");
                       
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error consuming stock for component: " + componentId, e);
            throw new RuntimeException("Failed to consume stock: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add stock (for imports)
     */
    public void addStock(Integer componentId, int quantity) {
        try {
            Components component = getComponentById(componentId);
            if (component == null) {
                throw new RuntimeException("Component not found: " + componentId);
            }
            
            int oldQuantity = component.getQuantity();
            int newQuantity = oldQuantity + quantity;
            
            component.setQuantity(newQuantity);
            component.setUpdatedAt(new Date());
            
            em.merge(component);
            em.flush();
            
            checkStockLevelsAndAlert(component, oldQuantity);
            
            logger.info("📈 Stock added for " + component.getName() + 
                       ": " + oldQuantity + " → " + newQuantity + " (added: " + quantity + ")");
                       
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error adding stock for component: " + componentId, e);
            throw new RuntimeException("Failed to add stock: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check stock levels and send alerts to MDB if thresholds are crossed
     */
    private void checkStockLevelsAndAlert(Components component, int oldQuantity) {
        try {
            int currentQuantity = component.getQuantity();
            int reorderLevel = component.getReorderLevel();
            
            // Critical stock: 50% of reorder level
            int criticalLevel = Math.max(1, reorderLevel / 2);
            
            // Only send alerts when crossing thresholds downward
            
            // OUT OF STOCK: Current = 0, Previous > 0
            if (currentQuantity == 0 && oldQuantity > 0) {
                logger.info("🔴 OUT_OF_STOCK triggered for: " + component.getName());
                notificationService.sendStockAlert(component.getComponentId(), "OUT_OF_STOCK");
            }
            
            // LOW STOCK: Current <= reorder level, Previous > reorder level
            else if (currentQuantity <= reorderLevel && oldQuantity > reorderLevel && currentQuantity > 0) {
                logger.info("⚠️ LOW_STOCK triggered for: " + component.getName());
                notificationService.sendStockAlert(component.getComponentId(), "LOW_STOCK");
            }
            
            // CRITICAL STOCK: Current <= critical level, Previous > critical level
            else if (currentQuantity <= criticalLevel && oldQuantity > criticalLevel && currentQuantity > 0) {
                logger.info("🚨 CRITICAL_STOCK triggered for: " + component.getName());
                notificationService.sendStockAlert(component.getComponentId(), "CRITICAL_STOCK");
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "⚠️ Error checking stock levels for component: " + 
                      component.getComponentId(), e);
        }
    }
    
    // =============== QUERY METHODS ===============
    
    /**
     * Get components by category
     */
    public List<Components> getComponentsByCategory(String category) {
        try {
            TypedQuery<Components> query = em.createNamedQuery("Components.findByCategory", Components.class);
            query.setParameter("category", category);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching components by category: " + category, e);
            throw new RuntimeException("Failed to fetch components by category: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get components with low stock
     */
    public List<Components> getLowStockComponents() {
        try {
            TypedQuery<Components> query = em.createQuery(
                "SELECT c FROM Components c WHERE c.quantity <= c.reorderLevel AND c.active = true ORDER BY c.quantity ASC", 
                Components.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching low stock components", e);
            throw new RuntimeException("Failed to fetch low stock components: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get components that are out of stock
     */
    public List<Components> getOutOfStockComponents() {
        try {
            TypedQuery<Components> query = em.createQuery(
                "SELECT c FROM Components c WHERE c.quantity = 0 AND c.active = true ORDER BY c.name", 
                Components.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching out of stock components", e);
            throw new RuntimeException("Failed to fetch out of stock components: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search components by name, part number, or manufacturer
     */
    public List<Components> searchComponents(String searchTerm) {
        try {
            TypedQuery<Components> query = em.createQuery(
                "SELECT c FROM Components c WHERE " +
                "(LOWER(c.name) LIKE LOWER(:searchTerm) OR " +
                "LOWER(c.partNumber) LIKE LOWER(:searchTerm) OR " +
                "LOWER(c.manufacturer) LIKE LOWER(:searchTerm)) " +
                "AND c.active = true ORDER BY c.name", 
                Components.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error searching components with term: " + searchTerm, e);
            throw new RuntimeException("Failed to search components: " + e.getMessage(), e);
        }
    }
    
    // =============== STATISTICS ===============
    
    /**
     * Get total number of active components
     */
    public long getTotalComponents() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Components c WHERE c.active = true", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error counting total components", e);
            return 0L;
        }
    }
    
    /**
     * Get count of low stock components
     */
    public long getLowStockCount() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Components c WHERE c.quantity <= c.reorderLevel AND c.active = true", 
                Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error counting low stock components", e);
            return 0L;
        }
    }
    
    /**
     * Find components below reorder level
     */
    public List<Components> findComponentsBelowReorderLevel() {
        try {
            TypedQuery<Components> query = em.createQuery(
                "SELECT c FROM Components c WHERE c.quantity <= c.reorderLevel AND c.active = true", 
                Components.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error finding components below reorder level", e);
            throw new RuntimeException("Failed to find components below reorder level: " + e.getMessage());
        }
    }
    
    /**
     * Get count of out of stock components
     */
    public long getOutOfStockCount() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Components c WHERE c.quantity = 0 AND c.active = true", 
                Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error counting out of stock components", e);
            return 0L;
        }
    }
    
    /**
     * Simulate stock consumption for MDB testing
     */
    public void simulateStockConsumption(Integer componentId, int consumeQuantity) {
        logger.info("🧪 Simulating stock consumption for component " + componentId + 
                   " - consuming " + consumeQuantity + " units");
        consumeStock(componentId, consumeQuantity);
    }
    
    /**
     * Get active components count
     */
    public long getActiveComponentsCount() {
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(c) FROM Components c", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.severe("Error getting active components count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get total inventory value
     */
    public BigDecimal getTotalInventoryValue() {
        try {
            TypedQuery<BigDecimal> query = em.createQuery(
                "SELECT SUM(c.quantityInStock * c.unitPrice) FROM Components c", BigDecimal.class);
            BigDecimal result = query.getSingleResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.severe("Error getting total inventory value: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Find out of stock components
     */
    public List<Components> findOutOfStockComponents() {
        try {
            TypedQuery<Components> query = em.createQuery(
                "SELECT c FROM Components c WHERE c.quantityInStock = 0 ORDER BY c.componentName", 
                Components.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding out of stock components: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Find components by category
     */
    public List<Components> findComponentsByCategory(String category) {
        try {
            TypedQuery<Components> query = em.createQuery(
                "SELECT c FROM Components c WHERE c.category = :category ORDER BY c.componentName", 
                Components.class);
            query.setParameter("category", category);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding components by category: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Find active components
     */
    public List<Components> findActiveComponents() {
        try {
            TypedQuery<Components> query = em.createQuery(
                "SELECT c FROM Components c WHERE c.quantityInStock > 0 ORDER BY c.componentName", 
                Components.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding active components: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Get recent stock alerts for testing
     */
    public List<entity.StockAlerts> getRecentStockAlerts(int maxResults) {
        try {
            TypedQuery<entity.StockAlerts> query = em.createQuery(
                "SELECT sa FROM StockAlerts sa ORDER BY sa.createdAt DESC", 
                entity.StockAlerts.class);
            query.setMaxResults(maxResults);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error getting recent stock alerts: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}