package managedbean;

import ejb.TransactionEJB;
import ejb.ComponentEJB;
import entity.Transactions;
import entity.Components;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Named
@RequestScoped
public class TransactionController implements Serializable {
    
    private static final Logger logger = Logger.getLogger(TransactionController.class.getName());
    
    @EJB
    private TransactionEJB transactionEJB;
    
    @EJB
    private ComponentEJB componentEJB;
    
    // Transaction form data
    private Integer selectedComponentId;
    private Integer selectedSupplierId;
    private String transactionType = "IMPORT";
    private int quantity;
    private BigDecimal unitPrice;
    private String notes;
    
    // Data lists
    private List<Transactions> transactions;
    private List<Transactions> recentTransactions;
    private List<SelectItem> componentOptions;
    private List<SelectItem> supplierOptions;
    private List<SelectItem> transactionTypeOptions;
    
    @PostConstruct
    public void init() {
        loadTransactions();
        loadComponentOptions();
        loadSupplierOptions();
        initTransactionTypeOptions();
    }
    
    // =============== TRANSACTION OPERATIONS ===============
    
    public void recordTransaction() {
        try {
            switch (transactionType) {
                case "IMPORT":
                    transactionEJB.recordImportTransaction(selectedComponentId, selectedSupplierId, 
                                                         quantity, unitPrice, notes);
                    addMessage("Success", "Import transaction recorded successfully!", FacesMessage.SEVERITY_INFO);
                    break;
                    
                case "EXPORT":
                    transactionEJB.recordExportTransaction(selectedComponentId, quantity, notes);
                    addMessage("Success", "Export transaction recorded successfully!", FacesMessage.SEVERITY_INFO);
                    break;
                    
                case "ADJUSTMENT":
                    transactionEJB.recordAdjustmentTransaction(selectedComponentId, quantity, notes);
                    addMessage("Success", "Adjustment transaction recorded successfully!", FacesMessage.SEVERITY_INFO);
                    break;
                    
                default:
                    addMessage("Error", "Invalid transaction type!", FacesMessage.SEVERITY_ERROR);
                    return;
            }
            
            // Reset form
            resetForm();
            loadTransactions();
            
        } catch (Exception e) {
            logger.severe("Error recording transaction: " + e.getMessage());
            addMessage("Error", "Failed to record transaction: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    private void resetForm() {
        selectedComponentId = null;
        selectedSupplierId = null;
        transactionType = "IMPORT";
        quantity = 0;
        unitPrice = null;
        notes = "";
    }
    
    // =============== DATA LOADING ===============
    
    private void loadTransactions() {
        try {
            transactions = transactionEJB.getAllTransactions();
            recentTransactions = transactionEJB.getRecentTransactions(10);
        } catch (Exception e) {
            logger.severe("Error loading transactions: " + e.getMessage());
            addMessage("Error", "Failed to load transactions: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    private void loadComponentOptions() {
        try {
            componentOptions = new ArrayList<>();
            componentOptions.add(new SelectItem(null, "-- Select Component --"));
            
            List<Components> components = componentEJB.getAllComponents();
            for (Components component : components) {
                componentOptions.add(new SelectItem(component.getComponentId(), 
                    component.getName() + " (" + component.getPartNumber() + ")"));
            }
        } catch (Exception e) {
            logger.severe("Error loading component options: " + e.getMessage());
        }
    }
    
    private void loadSupplierOptions() {
        try {
            supplierOptions = new ArrayList<>();
            supplierOptions.add(new SelectItem(null, "-- Select Supplier --"));
            
            // Note: You'll need to create SupplierEJB for this
            // For now, add some default options
            supplierOptions.add(new SelectItem(1, "Intel Corporation"));
            supplierOptions.add(new SelectItem(2, "AMD"));
            supplierOptions.add(new SelectItem(3, "Microchip Technology"));
            
        } catch (Exception e) {
            logger.severe("Error loading supplier options: " + e.getMessage());
        }
    }
    
    private void initTransactionTypeOptions() {
        transactionTypeOptions = new ArrayList<>();
        transactionTypeOptions.add(new SelectItem("IMPORT", "Import (Add Stock)"));
        transactionTypeOptions.add(new SelectItem("EXPORT", "Export (Remove Stock)"));
        transactionTypeOptions.add(new SelectItem("ADJUSTMENT", "Adjustment"));
    }
    
    private void addMessage(String summary, String detail, FacesMessage.Severity severity) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(severity, summary, detail));
    }
    
    // =============== GETTERS AND SETTERS ===============
    
    public Integer getSelectedComponentId() { return selectedComponentId; }
    public void setSelectedComponentId(Integer selectedComponentId) { this.selectedComponentId = selectedComponentId; }
    
    public Integer getSelectedSupplierId() { return selectedSupplierId; }
    public void setSelectedSupplierId(Integer selectedSupplierId) { this.selectedSupplierId = selectedSupplierId; }
    
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public List<Transactions> getTransactions() { return transactions; }
    public List<Transactions> getRecentTransactions() { return recentTransactions; }
    public List<SelectItem> getComponentOptions() { return componentOptions; }
    public List<SelectItem> getSupplierOptions() { return supplierOptions; }
    public List<SelectItem> getTransactionTypeOptions() { return transactionTypeOptions; }
}