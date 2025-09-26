/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package managedbean;

import ejb.ComponentEJB;
import ejb.ReorderEJB;
import ejb.SupplierEJB;
import ejb.TransactionEJB;
import ejb.UserEJB;
import entity.Components;
import entity.ReorderRequests;
import entity.Suppliers;
import entity.Transactions;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReportController - Handles reporting and analytics functionality
 * 
 * @author MINH_QUAN - Created on 2025-09-16
 */
@Named("reportController")
@RequestScoped
public class ReportController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ReportController.class.getName());
    
    @EJB
    private ComponentEJB componentEJB;
    
    @EJB
    private TransactionEJB transactionEJB;
    
    @EJB
    private ReorderEJB reorderEJB;
    
    @EJB
    private SupplierEJB supplierEJB;
    
    @EJB
    private UserEJB userEJB;
    
    
    
    // Report parameters
    private Date fromDate;
    private Date toDate;
    private String selectedCategory;
    private String selectedSupplier;
    private String reportType;
    
    // Report data
    private List<Components> lowStockComponents;
    private List<Components> outOfStockComponents;
    private List<Transactions> recentTransactions;
    private List<ReorderRequests> pendingReorders;
    private List<Suppliers> topSuppliers;
    
    // Analytics data
    private long totalComponents;
    private long activeComponents;
    private long lowStockCount;
    private long outOfStockCount;
    private BigDecimal totalInventoryValue;
    private long totalTransactions;
    private long totalSuppliers;
    private long totalUsers;
    
    @PostConstruct
    public void init() {
        logger.info("📊 ReportController initialized");
        
        // Set default date range (last 30 days)
        LocalDate now = LocalDate.now();
        toDate = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());
        fromDate = Date.from(now.minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        loadDashboardData();
    }
    
    /**
     * Load dashboard data
     */
    public void loadDashboardData() {
        try {
            logger.info("📊 Loading dashboard data");
            
            // Load basic statistics
            totalComponents = componentEJB.getTotalComponents();
            activeComponents = componentEJB.getActiveComponentsCount();
            lowStockCount = componentEJB.getLowStockCount();
            outOfStockCount = componentEJB.getOutOfStockCount();
            totalInventoryValue = componentEJB.getTotalInventoryValue();
            totalSuppliers = supplierEJB.getTotalSuppliers();
            totalUsers = userEJB.getTotalUsers();
            
            // Load lists
            lowStockComponents = componentEJB.findComponentsBelowReorderLevel();
            if (lowStockComponents.size() > 10) {
                lowStockComponents = lowStockComponents.subList(0, 10);
            }
            
            outOfStockComponents = componentEJB.findOutOfStockComponents();
            if (outOfStockComponents.size() > 10) {
                outOfStockComponents = outOfStockComponents.subList(0, 10);
            }
            
            pendingReorders = reorderEJB.findPendingReorderRequests();
            if (pendingReorders.size() > 10) {
                pendingReorders = pendingReorders.subList(0, 10);
            }
            
            topSuppliers = supplierEJB.getTopRatedSuppliers(5);
            
            logger.info("✅ Dashboard data loaded successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error loading dashboard data", e);
            addErrorMessage("Failed to load dashboard data");
        }
    }
    
    /**
     * Generate inventory report
     */
    public void generateInventoryReport() {
        try {
            logger.info("📋 Generating inventory report");
            
            List<Components> components;
            if (selectedCategory != null && !selectedCategory.trim().isEmpty()) {
                components = componentEJB.findComponentsByCategory(selectedCategory);
            } else {
                components = componentEJB.findActiveComponents();
            }
            
            // Process and display report data
            // In a real application, you might export to PDF or Excel
            addInfoMessage("Inventory report generated with " + components.size() + " components");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error generating inventory report", e);
            addErrorMessage("Failed to generate inventory report");
        }
    }
    
    /**
     * Generate transaction report
     */
    public void generateTransactionReport() {
        try {
            logger.info("📊 Generating transaction report for period: " + fromDate + " to " + toDate);
            
            if (fromDate == null || toDate == null) {
                addErrorMessage("Please select a valid date range");
                return;
            }
            
            if (fromDate.after(toDate)) {
                addErrorMessage("From date cannot be after to date");
                return;
            }
            
            List<Transactions> transactions = transactionEJB.findTransactionsByDateRange(fromDate, toDate);
            recentTransactions = transactions;
            
            addInfoMessage("Transaction report generated with " + transactions.size() + " transactions");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error generating transaction report", e);
            addErrorMessage("Failed to generate transaction report");
        }
    }
    
    /**
     * Generate supplier report
     */
    public void generateSupplierReport() {
        try {
            logger.info("🏢 Generating supplier report");
            
            List<Suppliers> suppliers = supplierEJB.findActiveSuppliers();
            topSuppliers = suppliers;
            
            addInfoMessage("Supplier report generated with " + suppliers.size() + " suppliers");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error generating supplier report", e);
            addErrorMessage("Failed to generate supplier report");
        }
    }
    
    /**
     * Get inventory status distribution for charts
     */
    public Map<String, Long> getInventoryStatusDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        try {
            distribution.put("In Stock", activeComponents - lowStockCount - outOfStockCount);
            distribution.put("Low Stock", lowStockCount);
            distribution.put("Out of Stock", outOfStockCount);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error getting inventory distribution", e);
        }
        return distribution;
    }
    
    /**
     * Get transaction trends for charts
     */
    public Map<String, Integer> getTransactionTrends() {
        Map<String, Integer> trends = new HashMap<>();
        try {
            // Simplified example - in real app, you'd calculate daily/weekly trends
            trends.put("IN", 150);
            trends.put("OUT", 120);
            trends.put("ADJUSTMENT", 10);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error getting transaction trends", e);
        }
        return trends;
    }
    
    /**
     * Get top categories by value
     */
    public Map<String, BigDecimal> getTopCategoriesByValue() {
        Map<String, BigDecimal> categories = new HashMap<>();
        try {
            categories.put("Microcontrollers", new BigDecimal("15000.00"));
            categories.put("Sensors", new BigDecimal("8500.00"));
            categories.put("Resistors", new BigDecimal("2500.00"));
            categories.put("Capacitors", new BigDecimal("3200.00"));
            categories.put("ICs", new BigDecimal("12000.00"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error getting categories by value", e);
        }
        return categories;
    }
    
    /**
     * Get system health status
     */
    public String getSystemHealthStatus() {
        try {
            double healthScore = 0.0;
            int factors = 0;
            
            // Factor 1: Stock levels
            if (outOfStockCount == 0) {
                healthScore += 30;
            } else if (outOfStockCount < 5) {
                healthScore += 20;
            } else if (outOfStockCount < 10) {
                healthScore += 10;
            }
            factors++;
            
            // Factor 2: Low stock levels
            if (lowStockCount < totalComponents * 0.1) {
                healthScore += 30;
            } else if (lowStockCount < totalComponents * 0.2) {
                healthScore += 20;
            } else {
                healthScore += 10;
            }
            factors++;
            
            // Factor 3: Pending reorders
            if (pendingReorders != null) {
                if (pendingReorders.size() == 0) {
                    healthScore += 20;
                } else if (pendingReorders.size() < 5) {
                    healthScore += 15;
                } else if (pendingReorders.size() < 10) {
                    healthScore += 10;
                } else {
                    healthScore += 5;
                }
            }
            factors++;
            
            // Factor 4: Active suppliers
            if (totalSuppliers > 10) {
                healthScore += 20;
            } else if (totalSuppliers > 5) {
                healthScore += 15;
            } else {
                healthScore += 10;
            }
            factors++;
            
            double finalScore = (healthScore / (factors * 30)) * 100;
            
            if (finalScore >= 80) {
                return "EXCELLENT";
            } else if (finalScore >= 60) {
                return "GOOD";
            } else if (finalScore >= 40) {
                return "FAIR";
            } else {
                return "POOR";
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error calculating system health", e);
            return "UNKNOWN";
        }
    }
    
    /**
     * Get health status CSS class
     */
    public String getHealthStatusClass() {
        String status = getSystemHealthStatus();
        return switch (status) {
            case "EXCELLENT" -> "text-success";
            case "GOOD" -> "text-info";
            case "FAIR" -> "text-warning";
            case "POOR" -> "text-danger";
            default -> "text-muted";
        };
    }
    
    /**
     * Export report to CSV (placeholder)
     */
    public void exportToCSV() {
        try {
            // In a real application, you would generate and download a CSV file
            addInfoMessage("CSV export functionality would be implemented here");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error exporting to CSV", e);
            addErrorMessage("Export failed");
        }
    }
    
    /**
     * Export report to PDF (placeholder)
     */
    public void exportToPDF() {
        try {
            // In a real application, you would generate and download a PDF file
            addInfoMessage("PDF export functionality would be implemented here");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error exporting to PDF", e);
            addErrorMessage("Export failed");
        }
    }
    
    /**
     * Add error message
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }
    
    /**
     * Add info message
     */
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", message));
    }
    
    // Getters and Setters
    public Date getFromDate() {
        return fromDate;
    }
    
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }
    
    public Date getToDate() {
        return toDate;
    }
    
    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
    
    public String getSelectedCategory() {
        return selectedCategory;
    }
    
    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }
    
    public String getSelectedSupplier() {
        return selectedSupplier;
    }
    
    public void setSelectedSupplier(String selectedSupplier) {
        this.selectedSupplier = selectedSupplier;
    }
    
    public String getReportType() {
        return reportType;
    }
    
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    
    public List<Components> getLowStockComponents() {
        return lowStockComponents;
    }
    
    public List<Components> getOutOfStockComponents() {
        return outOfStockComponents;
    }
    
    public List<Transactions> getRecentTransactions() {
        return recentTransactions;
    }
    
    public List<ReorderRequests> getPendingReorders() {
        return pendingReorders;
    }
    
    public List<Suppliers> getTopSuppliers() {
        return topSuppliers;
    }
    
    public long getTotalComponents() {
        return totalComponents;
    }
    
    public long getActiveComponents() {
        return activeComponents;
    }
    
    public long getLowStockCount() {
        return lowStockCount;
    }
    
    public long getOutOfStockCount() {
        return outOfStockCount;
    }
    
    public BigDecimal getTotalInventoryValue() {
        return totalInventoryValue;
    }
    
    public long getTotalTransactions() {
        return totalTransactions;
    }
    
    public long getTotalSuppliers() {
        return totalSuppliers;
    }
    
    public long getTotalUsers() {
        return totalUsers;
    }
}