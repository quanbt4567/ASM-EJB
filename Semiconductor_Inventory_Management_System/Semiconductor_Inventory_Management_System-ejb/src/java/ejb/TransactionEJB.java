package ejb;

import entity.Components;
import entity.Suppliers;
import entity.Transactions;
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
public class TransactionEJB {
    
    private static final Logger logger = Logger.getLogger(TransactionEJB.class.getName());
    
    @PersistenceContext(unitName = "semiconductor-pu")
    private EntityManager em;
    
    @EJB
    private ComponentEJB componentEJB;
    
    // =============== TRANSACTION OPERATIONS ===============
    
    /**
     * Record an IMPORT transaction (adding stock)
     */
    public void recordImportTransaction(Integer componentId, Integer supplierId, int quantity, 
                                      BigDecimal unitPrice, String notes) {
        try {
            Components component = em.find(Components.class, componentId);
            if (component == null) {
                throw new RuntimeException("Component not found: " + componentId);
            }
            
            Suppliers supplier = null;
            if (supplierId != null) {
                supplier = em.find(Suppliers.class, supplierId);
            }
            
            Users defaultUser = em.find(Users.class, 1); // Default admin user
            
            // Create transaction
            Transactions transaction = new Transactions();
            transaction.setComponentId(component);
            transaction.setSupplierId(supplier);
            transaction.setUserId(defaultUser);
            transaction.setType("IMPORT");
            transaction.setQuantity(quantity);
            transaction.setUnitPrice(unitPrice);
            
            if (unitPrice != null) {
                transaction.setTotalValue(unitPrice.multiply(BigDecimal.valueOf(quantity)));
            }
            
            Date now = new Date();
            transaction.setTransactionDate(now);
            transaction.setCreatedAt(now);
            transaction.setReferenceNumber(generateReferenceNumber("IMPORT"));
            transaction.setNotes(notes);
            
            em.persist(transaction);
            em.flush();
            
            // Update component stock
            componentEJB.addStock(componentId, quantity);
            
            logger.info("✅ IMPORT transaction recorded: " + quantity + " units of " + 
                       component.getName() + " (Ref: " + transaction.getReferenceNumber() + ")");
                       
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error recording import transaction", e);
            throw new RuntimeException("Failed to record import transaction: " + e.getMessage(), e);
        }
    }
    
    /**
     * Record an EXPORT transaction (consuming stock)
     */
    public void recordExportTransaction(Integer componentId, int quantity, String notes) {
        try {
            Components component = em.find(Components.class, componentId);
            if (component == null) {
                throw new RuntimeException("Component not found: " + componentId);
            }
            
            if (component.getQuantity() < quantity) {
                logger.warning("⚠️ Insufficient stock for " + component.getName() + 
                             ". Available: " + component.getQuantity() + ", Requested: " + quantity);
                // Still allow the transaction but warn
            }
            
            Users defaultUser = em.find(Users.class, 1);
            
            // Create transaction
            Transactions transaction = new Transactions();
            transaction.setComponentId(component);
            transaction.setUserId(defaultUser);
            transaction.setType("EXPORT");
            transaction.setQuantity(quantity);
            transaction.setUnitPrice(component.getUnitPrice());
            
            if (component.getUnitPrice() != null) {
                transaction.setTotalValue(component.getUnitPrice().multiply(BigDecimal.valueOf(quantity)).negate());
            }
            
            Date now = new Date();
            transaction.setTransactionDate(now);
            transaction.setCreatedAt(now);
            transaction.setReferenceNumber(generateReferenceNumber("EXPORT"));
            transaction.setNotes(notes);
            
            em.persist(transaction);
            em.flush();
            
            // Update component stock
            componentEJB.consumeStock(componentId, quantity);
            
            logger.info("✅ EXPORT transaction recorded: " + quantity + " units of " + 
                       component.getName() + " (Ref: " + transaction.getReferenceNumber() + ")");
                       
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error recording export transaction", e);
            throw new RuntimeException("Failed to record export transaction: " + e.getMessage(), e);
        }
    }
    
    /**
     * Record an ADJUSTMENT transaction
     */
    public void recordAdjustmentTransaction(Integer componentId, int adjustmentQuantity, String reason) {
        try {
            Components component = em.find(Components.class, componentId);
            if (component == null) {
                throw new RuntimeException("Component not found: " + componentId);
            }
            
            Users defaultUser = em.find(Users.class, 1);
            
            // Create transaction
            Transactions transaction = new Transactions();
            transaction.setComponentId(component);
            transaction.setUserId(defaultUser);
            transaction.setType("ADJUSTMENT");
            transaction.setQuantity(adjustmentQuantity);
            transaction.setUnitPrice(component.getUnitPrice());
            
            if (component.getUnitPrice() != null) {
                transaction.setTotalValue(component.getUnitPrice().multiply(BigDecimal.valueOf(adjustmentQuantity)));
            }
            
            Date now = new Date();
            transaction.setTransactionDate(now);
            transaction.setCreatedAt(now);
            transaction.setReferenceNumber(generateReferenceNumber("ADJUSTMENT"));
            transaction.setNotes(reason);
            
            em.persist(transaction);
            em.flush();
            
            // Update component stock
            if (adjustmentQuantity > 0) {
                componentEJB.addStock(componentId, adjustmentQuantity);
            } else {
                componentEJB.consumeStock(componentId, Math.abs(adjustmentQuantity));
            }
            
            logger.info("✅ ADJUSTMENT transaction recorded: " + adjustmentQuantity + " units of " + 
                       component.getName() + " (Ref: " + transaction.getReferenceNumber() + ")");
                       
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error recording adjustment transaction", e);
            throw new RuntimeException("Failed to record adjustment transaction: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate unique reference number
     */
    private String generateReferenceNumber(String type) {
        String prefix;
        switch (type) {
            case "IMPORT": prefix = "IMP"; break;
            case "EXPORT": prefix = "EXP"; break;
            case "ADJUSTMENT": prefix = "ADJ"; break;
            case "RETURN": prefix = "RET"; break;
            default: prefix = "TXN";
        }
        
        return prefix + "-" + System.currentTimeMillis() + "-" + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
    
    // =============== QUERY OPERATIONS ===============
    
    /**
     * Get all transactions
     */
    public List<Transactions> getAllTransactions() {
        try {
            TypedQuery<Transactions> query = em.createNamedQuery("Transactions.findAll", Transactions.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching all transactions", e);
            throw new RuntimeException("Failed to fetch transactions: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get transactions by type
     */
    public List<Transactions> getTransactionsByType(String type) {
        try {
            TypedQuery<Transactions> query = em.createNamedQuery("Transactions.findByType", Transactions.class);
            query.setParameter("type", type);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching transactions by type: " + type, e);
            throw new RuntimeException("Failed to fetch transactions by type: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get recent transactions
     */
    public List<Transactions> getRecentTransactions(int limit) {
        try {
            TypedQuery<Transactions> query = em.createQuery(
                "SELECT t FROM Transactions t ORDER BY t.transactionDate DESC", 
                Transactions.class);
            query.setMaxResults(limit);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching recent transactions", e);
            throw new RuntimeException("Failed to fetch recent transactions: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get transactions for a specific component
     */
    public List<Transactions> getTransactionsByComponent(Integer componentId) {
        try {
            Components component = em.find(Components.class, componentId);
            if (component == null) {
                throw new RuntimeException("Component not found: " + componentId);
            }
            
            TypedQuery<Transactions> query = em.createQuery(
                "SELECT t FROM Transactions t WHERE t.componentId = :component ORDER BY t.transactionDate DESC", 
                Transactions.class);
            query.setParameter("component", component);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error fetching transactions for component: " + componentId, e);
            throw new RuntimeException("Failed to fetch transactions for component: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find transactions within date range
     */
    public List<Transactions> findTransactionsByDateRange(Date fromDate, Date toDate) {
        try {
            TypedQuery<Transactions> query = em.createQuery(
                "SELECT t FROM Transactions t WHERE t.transactionDate BETWEEN :fromDate AND :toDate ORDER BY t.transactionDate DESC", 
                Transactions.class);
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);
            return query.getResultList();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error finding transactions by date range", e);
            return new java.util.ArrayList<>();
        }
    }
    
    // =============== TESTING METHODS ===============
    
    /**
     * Test method to trigger MDB alerts
     */
    public void testMDBWithStockConsumption(Integer componentId, int consumeQuantity) {
        try {
            logger.info("🧪 Testing MDB with stock consumption - Component: " + componentId + 
                       ", Consume: " + consumeQuantity + " units");
            
            recordExportTransaction(componentId, consumeQuantity, "MDB Test - Stock consumption simulation");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error in MDB test", e);
            throw new RuntimeException("Failed to test MDB: " + e.getMessage(), e);
        }
    }
}