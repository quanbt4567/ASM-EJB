package managedbean;

import ejb.ComponentEJB;
import ejb.TransactionEJB;
import entity.Components;
import entity.Transactions;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

@Named
@RequestScoped
public class DashboardController implements Serializable {
    
    private static final Logger logger = Logger.getLogger(DashboardController.class.getName());
    
    @EJB
    private ComponentEJB componentEJB;
    
    @EJB
    private TransactionEJB transactionEJB;
    
    // Dashboard statistics
    private long totalComponents;
    private long lowStockCount;
    private long outOfStockCount;
    private long healthyStockCount;
    
    // Recent data
    private List<Components> lowStockComponents;
    private List<Components> outOfStockComponents;
    private List<Transactions> recentTransactions;
    
    @PostConstruct
    public void init() {
        loadDashboardData();
    }
    
    public void loadDashboardData() {
        try {
            // Load statistics
            totalComponents = componentEJB.getTotalComponents();
            lowStockCount = componentEJB.getLowStockCount();
            outOfStockCount = componentEJB.getOutOfStockCount();
            healthyStockCount = totalComponents - lowStockCount;
            
            // Load alerts
            lowStockComponents = componentEJB.getLowStockComponents();
            outOfStockComponents = componentEJB.getOutOfStockComponents();
            
            // Load recent transactions
            recentTransactions = transactionEJB.getRecentTransactions(10);
            
            logger.info("Dashboard data loaded successfully");
            
        } catch (Exception e) {
            logger.severe("Error loading dashboard data: " + e.getMessage());
        }
    }
    
    public void refreshDashboard() {
        loadDashboardData();
    }
    
    // =============== GETTERS ===============
    
    public long getTotalComponents() { return totalComponents; }
    public long getLowStockCount() { return lowStockCount; }
    public long getOutOfStockCount() { return outOfStockCount; }
    public long getHealthyStockCount() { return healthyStockCount; }
    
    public List<Components> getLowStockComponents() { return lowStockComponents; }
    public List<Components> getOutOfStockComponents() { return outOfStockComponents; }
    public List<Transactions> getRecentTransactions() { return recentTransactions; }
    
    // =============== CALCULATED PROPERTIES ===============
    
    public double getLowStockPercentage() {
        return totalComponents > 0 ? (double) lowStockCount / totalComponents * 100 : 0;
    }
    
    public double getHealthyStockPercentage() {
        return totalComponents > 0 ? (double) healthyStockCount / totalComponents * 100 : 0;
    }
    
    public String getSystemHealthStatus() {
        if (outOfStockCount > 0) {
            return "CRITICAL";
        } else if (lowStockCount > totalComponents * 0.3) {
            return "WARNING";
        } else {
            return "HEALTHY";
        }
    }
    
    public String getSystemHealthColor() {
        switch (getSystemHealthStatus()) {
            case "CRITICAL": return "red";
            case "WARNING": return "orange";
            default: return "green";
        }
    }
}