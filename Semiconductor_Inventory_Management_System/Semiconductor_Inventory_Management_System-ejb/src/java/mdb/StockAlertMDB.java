package mdb;

import entity.Components;
import entity.StockAlerts;
import entity.Users;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(
    name = "StockAlertMDB",
    activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/StockAlertQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
    }
)
public class StockAlertMDB implements MessageListener {
    
    private static final Logger logger = Logger.getLogger(StockAlertMDB.class.getName());
    
    @PersistenceContext(unitName = "semiconductor-pu")
    private EntityManager em;
    
    @Override
    public void onMessage(Message message) {
        logger.info("🔔 MDB received message: " + message.getClass().getSimpleName());
        
        try {
            if (message instanceof MapMessage) {
                MapMessage mapMessage = (MapMessage) message;
                processStockAlert(mapMessage);
            } else {
                logger.warning("⚠️ MDB received unsupported message type: " + message.getClass());
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error processing message in MDB", e);
            // In real application, you might want to send to dead letter queue
        }
    }
    
    /**
     * Process stock alert message
     */
    private void processStockAlert(MapMessage message) throws JMSException {
        try {
            // Extract message data
            Integer componentId = message.getInt("componentId");
            String alertType = message.getString("alertType");
            long timestamp = message.getLong("timestamp");
            String source = message.getString("source");
            
            logger.info("🔍 Processing " + alertType + " alert for component " + componentId + 
                       " from " + source);
            
            // Get component details
            Components component = em.find(Components.class, componentId);
            if (component == null) {
                logger.warning("⚠️ Component not found for alert: " + componentId);
                return;
            }
            
            // Create stock alert record
            StockAlerts stockAlert = new StockAlerts();
            stockAlert.setComponentId(component);
            stockAlert.setAlertType(alertType);
            stockAlert.setCreatedAt(new Date(timestamp));
            stockAlert.setProcessed(false);
            stockAlert.setEmailSent(false);
            stockAlert.setResolved(false);
            
            // Set severity and description based on alert type
            switch (alertType) {
                case "OUT_OF_STOCK":
                    stockAlert.setSeverity("CRITICAL");
                    stockAlert.setDescription(component.getName() + " is completely out of stock! " +
                                            "Current quantity: " + component.getQuantity() + 
                                            ", Reorder level: " + component.getReorderLevel());
                    break;
                    
                case "LOW_STOCK":
                    stockAlert.setSeverity("HIGH");
                    stockAlert.setDescription(component.getName() + " stock is below reorder level. " +
                                            "Current quantity: " + component.getQuantity() + 
                                            ", Reorder level: " + component.getReorderLevel());
                    break;
                    
                case "CRITICAL_STOCK":
                    stockAlert.setSeverity("CRITICAL");
                    stockAlert.setDescription(component.getName() + " stock is critically low! " +
                                            "Current quantity: " + component.getQuantity() + 
                                            ", Reorder level: " + component.getReorderLevel());
                    break;
                    
                default:
                    stockAlert.setSeverity("MEDIUM");
                    stockAlert.setDescription("Stock alert for " + component.getName());
            }
            
            // Persist alert to database
            em.persist(stockAlert);
            em.flush();
            
            // Log successful processing
            logger.info("✅ MDB successfully processed " + alertType + " alert for " + 
                       component.getName() + " (Alert ID: " + stockAlert.getAlertId() + ")");
            
            // Simulate email notification
            simulateEmailNotification(stockAlert, component);
            
            // Mark as processed
            stockAlert.setProcessed(true);
            stockAlert.setProcessedAt(new Date());
            stockAlert.setEmailSent(true);
            
            Users defaultUser = em.find(Users.class, 1);
            if (defaultUser != null) {
                stockAlert.setProcessedBy(defaultUser);
            }
            
            em.merge(stockAlert);
            em.flush();
            
            logger.info("📧 Email notification sent and alert marked as processed");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error in MDB processStockAlert", e);
            throw new RuntimeException("MDB processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Simulate email notification (in real app, integrate with email service)
     */
    private void simulateEmailNotification(StockAlerts alert, Components component) {
        try {
            String emailContent = generateEmailContent(alert, component);
            
            // In real application, you would integrate with email service here
            // For demo purposes, we'll just log the email content
            logger.info("📧 EMAIL NOTIFICATION SENT:");
            logger.info("To: inventory-manager@semiconductor.com");
            logger.info("Subject: " + alert.getSeverity() + " Stock Alert - " + component.getName());
            logger.info("Content: " + emailContent);
            logger.info("═══════════════════════════════════════════════════════");
            
            // Simulate email processing delay
            Thread.sleep(100);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "⚠️ Error simulating email notification", e);
        }
    }
    
    /**
     * Generate email content for alerts
     */
    private String generateEmailContent(StockAlerts alert, Components component) {
        StringBuilder content = new StringBuilder();
        
        content.append("SEMICONDUCTOR INVENTORY ALERT\n");
        content.append("════════════════════════════════\n\n");
        
        content.append("Alert Type: ").append(alert.getAlertType()).append("\n");
        content.append("Severity: ").append(alert.getSeverity()).append("\n");
        content.append("Component: ").append(component.getName()).append("\n");
        content.append("Part Number: ").append(component.getPartNumber() != null ? component.getPartNumber() : "N/A").append("\n");
        content.append("Manufacturer: ").append(component.getManufacturer() != null ? component.getManufacturer() : "N/A").append("\n");
        content.append("Current Stock: ").append(component.getQuantity()).append(" units\n");
        content.append("Reorder Level: ").append(component.getReorderLevel()).append(" units\n");
        content.append("Location: ").append(component.getLocation() != null ? component.getLocation() : "N/A").append("\n");
        content.append("Category: ").append(component.getCategory()).append("\n");
        
        content.append("\nDescription:\n");
        content.append(alert.getDescription()).append("\n\n");
        
        content.append("Recommended Actions:\n");
        switch (alert.getAlertType()) {
            case "OUT_OF_STOCK":
                content.append("• URGENT: Place immediate order with supplier\n");
                content.append("• Contact production team about potential delays\n");
                content.append("• Check for alternative components\n");
                break;
            case "LOW_STOCK":
                content.append("• Schedule reorder before stock runs out\n");
                content.append("• Review reorder quantities and lead times\n");
                content.append("• Verify supplier availability\n");
                break;
            case "CRITICAL_STOCK":
                content.append("• CRITICAL: Emergency reorder required\n");
                content.append("• Consider expedited shipping\n");
                content.append("• Notify production planning team\n");
                break;
        }
        
        content.append("\nAlert Generated: ").append(alert.getCreatedAt()).append("\n");
        content.append("System: Semiconductor Inventory Management\n");
        
        return content.toString();
    }
}