package ejb;

import entity.ReorderRequests;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class NotificationService {
    
    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());
    
    @Resource(lookup = "jms/__defaultConnectionFactory")
    private ConnectionFactory connectionFactory;
    
    @Resource(lookup = "jms/StockAlertQueue")
    private Queue stockAlertQueue;
    
    /**
     * Send stock alert message to MDB
     */
    public void sendStockAlert(Integer componentId, String alertType) {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        
        try {
            // Create JMS connection and session
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(stockAlertQueue);
            
            // Create message
            MapMessage message = session.createMapMessage();
            message.setInt("componentId", componentId);
            message.setString("alertType", alertType);
            message.setLong("timestamp", System.currentTimeMillis());
            message.setString("source", "ComponentEJB");
            
            // Send message to queue
            producer.send(message);
            
            logger.info("📤 Stock alert sent to MDB: " + alertType + " for component " + componentId);
            
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "❌ Error sending stock alert to MDB", e);
            throw new RuntimeException("Failed to send stock alert: " + e.getMessage(), e);
        } finally {
            // Clean up resources
            try {
                if (producer != null) producer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                logger.log(Level.WARNING, "⚠️ Error closing JMS resources", e);
            }
        }
    }
    
    /**
     * Send custom alert with additional data
     */
    public void sendCustomAlert(Integer componentId, String alertType, String description, int currentQuantity, int reorderLevel) {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(stockAlertQueue);
            
            MapMessage message = session.createMapMessage();
            message.setInt("componentId", componentId);
            message.setString("alertType", alertType);
            message.setString("description", description);
            message.setInt("currentQuantity", currentQuantity);
            message.setInt("reorderLevel", reorderLevel);
            message.setLong("timestamp", System.currentTimeMillis());
            message.setString("source", "NotificationService");
            
            producer.send(message);
            
            logger.info("📤 Custom alert sent to MDB: " + alertType + " for component " + componentId + 
                       " - " + description);
            
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "❌ Error sending custom alert to MDB", e);
            throw new RuntimeException("Failed to send custom alert: " + e.getMessage(), e);
        } finally {
            try {
                if (producer != null) producer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                logger.log(Level.WARNING, "⚠️ Error closing JMS resources", e);
            }
        }
    }
    
    /**
     * Send reorder notification
     */
    public void sendReorderNotification(ReorderRequests request) {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(stockAlertQueue);
            
            MapMessage message = session.createMapMessage();
            message.setInt("requestId", request.getRequestId());
            message.setString("alertType", "REORDER_REQUEST");
            message.setString("componentName", request.getComponentId().getName());
            message.setInt("requestedQuantity", request.getRequestedQuantity());
            message.setString("priority", request.getPriority());
            message.setLong("timestamp", System.currentTimeMillis());
            message.setString("source", "ReorderEJB");
            
            producer.send(message);
            
            logger.info("📤 Reorder notification sent for request ID: " + request.getRequestId());
            
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "❌ Error sending reorder notification", e);
            throw new RuntimeException("Failed to send reorder notification: " + e.getMessage(), e);
        } finally {
            try {
                if (producer != null) producer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                logger.log(Level.WARNING, "⚠️ Error closing JMS resources", e);
            }
        }
    }
    
    /**
     * Send reorder approval notification
     */
    public void sendReorderApprovalNotification(ReorderRequests request) {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(stockAlertQueue);
            
            MapMessage message = session.createMapMessage();
            message.setInt("requestId", request.getRequestId());
            message.setString("alertType", "REORDER_APPROVED");
            message.setString("componentName", request.getComponentId().getName());
            message.setString("approvedBy", request.getApprovedBy().getUsername());
            message.setLong("timestamp", System.currentTimeMillis());
            message.setString("source", "ReorderEJB");
            
            producer.send(message);
            
            logger.info("📤 Reorder approval notification sent for request ID: " + request.getRequestId());
            
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "❌ Error sending reorder approval notification", e);
            throw new RuntimeException("Failed to send reorder approval notification: " + e.getMessage(), e);
        } finally {
            try {
                if (producer != null) producer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                logger.log(Level.WARNING, "⚠️ Error closing JMS resources", e);
            }
        }
    }
}