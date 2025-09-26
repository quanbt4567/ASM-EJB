package managedbean;

import ejb.SupplierEJB;
import entity.Suppliers;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

@Named
@RequestScoped
public class SupplierController implements Serializable {
    
    private static final Logger logger = Logger.getLogger(SupplierController.class.getName());
    
    @EJB
    private SupplierEJB supplierEJB;
    
    private Suppliers selectedSupplier;
    private Suppliers newSupplier;
    private List<Suppliers> suppliers;
    private String searchTerm;
    private String selectedCountry;
    
    @PostConstruct
    public void init() {
        newSupplier = new Suppliers();
        loadSuppliers();
    }
    
    // =============== CRUD OPERATIONS ===============
    
    public void createSupplier() {
        try {
            if (!supplierEJB.isSupplierEmailUnique(newSupplier.getEmail(), null)) {
                addMessage("Error", "Email already exists!", FacesMessage.SEVERITY_ERROR);
                return;
            }
            
            supplierEJB.createSupplier(newSupplier);
            addMessage("Success", "Supplier created successfully!", FacesMessage.SEVERITY_INFO);
            
            newSupplier = new Suppliers();
            loadSuppliers();
            
        } catch (Exception e) {
            logger.severe("Error creating supplier: " + e.getMessage());
            addMessage("Error", "Failed to create supplier: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    public void updateSupplier() {
        try {
            if (!supplierEJB.isSupplierEmailUnique(selectedSupplier.getEmail(), 
                                                  selectedSupplier.getSupplierId())) {
                addMessage("Error", "Email already exists!", FacesMessage.SEVERITY_ERROR);
                return;
            }
            
            supplierEJB.updateSupplier(selectedSupplier);
            addMessage("Success", "Supplier updated successfully!", FacesMessage.SEVERITY_INFO);
            loadSuppliers();
            
        } catch (Exception e) {
            logger.severe("Error updating supplier: " + e.getMessage());
            addMessage("Error", "Failed to update supplier: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    public void deleteSupplier(Suppliers supplier) {
        try {
            supplierEJB.deleteSupplier(supplier.getSupplierId());
            addMessage("Success", "Supplier deleted successfully!", FacesMessage.SEVERITY_INFO);
            loadSuppliers();
            
        } catch (Exception e) {
            logger.severe("Error deleting supplier: " + e.getMessage());
            addMessage("Error", "Failed to delete supplier: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    // =============== SEARCH AND FILTER ===============
    
    public void searchSuppliers() {
        try {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                suppliers = supplierEJB.searchSuppliers(searchTerm);
            } else {
                loadSuppliers();
            }
        } catch (Exception e) {
            logger.severe("Error searching suppliers: " + e.getMessage());
            addMessage("Error", "Search failed: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    public void filterByCountry() {
        try {
            if (selectedCountry != null && !selectedCountry.trim().isEmpty()) {
                suppliers = supplierEJB.getSuppliersByCountry(selectedCountry);
            } else {
                loadSuppliers();
            }
        } catch (Exception e) {
            logger.severe("Error filtering by country: " + e.getMessage());
            addMessage("Error", "Filter failed: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    // =============== UTILITY METHODS ===============
    
    private void loadSuppliers() {
        try {
            suppliers = supplierEJB.getAllSuppliers();
        } catch (Exception e) {
            logger.severe("Error loading suppliers: " + e.getMessage());
            addMessage("Error", "Failed to load suppliers: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    private void addMessage(String summary, String detail, FacesMessage.Severity severity) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(severity, summary, detail));
    }
    
    // =============== GETTERS AND SETTERS ===============
    
    public Suppliers getSelectedSupplier() { return selectedSupplier; }
    public void setSelectedSupplier(Suppliers selectedSupplier) { this.selectedSupplier = selectedSupplier; }
    
    public Suppliers getNewSupplier() { return newSupplier; }
    public void setNewSupplier(Suppliers newSupplier) { this.newSupplier = newSupplier; }
    
    public List<Suppliers> getSuppliers() { return suppliers; }
    public void setSuppliers(List<Suppliers> suppliers) { this.suppliers = suppliers; }
    
    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
    
    public String getSelectedCountry() { return selectedCountry; }
    public void setSelectedCountry(String selectedCountry) { this.selectedCountry = selectedCountry; }
}