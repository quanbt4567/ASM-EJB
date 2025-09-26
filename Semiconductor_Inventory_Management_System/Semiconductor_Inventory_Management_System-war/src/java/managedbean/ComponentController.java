package managedbean;

import ejb.ComponentEJB;
import entity.Components;
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
public class ComponentController implements Serializable {
    
    private static final Logger logger = Logger.getLogger(ComponentController.class.getName());
    
    @EJB
    private ComponentEJB componentEJB;
    
    private Components selectedComponent;
    private Components newComponent;
    private List<Components> components;
    private List<Components> lowStockComponents;
    private List<Components> outOfStockComponents;
    private String searchTerm;
    private String selectedCategory;
    
    @PostConstruct
    public void init() {
        newComponent = new Components();
        loadComponents();
        loadStockAlerts();
    }
    
    // =============== CRUD OPERATIONS ===============
    
    public void createComponent() {
        try {
            componentEJB.createComponent(newComponent);
            addMessage("Success", "Component created successfully!", FacesMessage.SEVERITY_INFO);
            
            // Reset form
            newComponent = new Components();
            loadComponents();
            
        } catch (Exception e) {
            logger.severe("Error creating component: " + e.getMessage());
            addMessage("Error", "Failed to create component: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    public void updateComponent() {
        try {
            componentEJB.updateComponent(selectedComponent);
            addMessage("Success", "Component updated successfully!", FacesMessage.SEVERITY_INFO);
            loadComponents();
            
        } catch (Exception e) {
            logger.severe("Error updating component: " + e.getMessage());
            addMessage("Error", "Failed to update component: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    public void deleteComponent(Components component) {
        try {
            componentEJB.deleteComponent(component.getComponentId());
            addMessage("Success", "Component deleted successfully!", FacesMessage.SEVERITY_INFO);
            loadComponents();
            
        } catch (Exception e) {
            logger.severe("Error deleting component: " + e.getMessage());
            addMessage("Error", "Failed to delete component: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    // =============== SEARCH AND FILTER ===============
    
    public void searchComponents() {
        try {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                components = componentEJB.searchComponents(searchTerm);
            } else {
                loadComponents();
            }
        } catch (Exception e) {
            logger.severe("Error searching components: " + e.getMessage());
            addMessage("Error", "Search failed: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    public void filterByCategory() {
        try {
            if (selectedCategory != null && !selectedCategory.trim().isEmpty()) {
                components = componentEJB.getComponentsByCategory(selectedCategory);
            } else {
                loadComponents();
            }
        } catch (Exception e) {
            logger.severe("Error filtering by category: " + e.getMessage());
            addMessage("Error", "Filter failed: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    // =============== STOCK MANAGEMENT ===============
    
    public void updateStock(Components component, int newQuantity) {
        try {
            componentEJB.updateStock(component.getComponentId(), newQuantity);
            addMessage("Success", "Stock updated for " + component.getName(), FacesMessage.SEVERITY_INFO);
            loadComponents();
            loadStockAlerts();
            
        } catch (Exception e) {
            logger.severe("Error updating stock: " + e.getMessage());
            addMessage("Error", "Failed to update stock: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    public void addStock(Components component, int quantity) {
        try {
            componentEJB.addStock(component.getComponentId(), quantity);
            addMessage("Success", "Added " + quantity + " units to " + component.getName(), FacesMessage.SEVERITY_INFO);
            loadComponents();
            loadStockAlerts();
            
        } catch (Exception e) {
            logger.severe("Error adding stock: " + e.getMessage());
            addMessage("Error", "Failed to add stock: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    public void consumeStock(Components component, int quantity) {
        try {
            componentEJB.consumeStock(component.getComponentId(), quantity);
            addMessage("Success", "Consumed " + quantity + " units from " + component.getName(), FacesMessage.SEVERITY_INFO);
            loadComponents();
            loadStockAlerts();
            
        } catch (Exception e) {
            logger.severe("Error consuming stock: " + e.getMessage());
            addMessage("Error", "Failed to consume stock: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    // =============== UTILITY METHODS ===============
    
    private void loadComponents() {
        try {
            components = componentEJB.getAllComponents();
        } catch (Exception e) {
            logger.severe("Error loading components: " + e.getMessage());
            addMessage("Error", "Failed to load components: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }
    
    private void loadStockAlerts() {
        try {
            lowStockComponents = componentEJB.getLowStockComponents();
            outOfStockComponents = componentEJB.getOutOfStockComponents();
        } catch (Exception e) {
            logger.severe("Error loading stock alerts: " + e.getMessage());
        }
    }
    
    private void addMessage(String summary, String detail, FacesMessage.Severity severity) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(severity, summary, detail));
    }
    
    public String getStockStatus(Components component) {
        if (component.getQuantity() == 0) {
            return "OUT_OF_STOCK";
        } else if (component.getQuantity() <= component.getReorderLevel()) {
            return "LOW_STOCK";
        } else {
            return "NORMAL";
        }
    }
    
    public String getStockStatusColor(Components component) {
        switch (getStockStatus(component)) {
            case "OUT_OF_STOCK": return "red";
            case "LOW_STOCK": return "orange";
            default: return "green";
        }
    }
    
    // =============== GETTERS AND SETTERS ===============
    
    public Components getSelectedComponent() { return selectedComponent; }
    public void setSelectedComponent(Components selectedComponent) { this.selectedComponent = selectedComponent; }
    
    public Components getNewComponent() { return newComponent; }
    public void setNewComponent(Components newComponent) { this.newComponent = newComponent; }
    
    public List<Components> getComponents() { return components; }
    public void setComponents(List<Components> components) { this.components = components; }
    
    public List<Components> getLowStockComponents() { return lowStockComponents; }
    public void setLowStockComponents(List<Components> lowStockComponents) { this.lowStockComponents = lowStockComponents; }
    
    public List<Components> getOutOfStockComponents() { return outOfStockComponents; }
    public void setOutOfStockComponents(List<Components> outOfStockComponents) { this.outOfStockComponents = outOfStockComponents; }
    
    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
    
    public String getSelectedCategory() { return selectedCategory; }
    public void setSelectedCategory(String selectedCategory) { this.selectedCategory = selectedCategory; }
}