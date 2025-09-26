/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mdb;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReorderNotificationMDB - Message-Driven Bean for processing reorder notifications
 * Handles email notifications for reorder requests and approvals
 * 
 * @author MINH_QUAN - Created on 2025-09-16
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/ReorderNotificationQueue"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
})
public class ReorderNotificationMDB implements MessageListener {
    
    private static final Logger logger = Logger.getLogger(ReorderNotificationMDB.class.getName());
    
    // Email configuration - in production, these should come from JNDI or configuration
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String FROM_EMAIL = "noreply@semiconductor-inventory.com";
    
    @Override
    public void onMessage(Message message) {
        logger.info("🔔 ReorderNotificationMDB received message");
        
        try {
            if (message instanceof MapMessage) {
                MapMessage mapMessage = (MapMessage) message;
                processReorderNotification(mapMessage);
            } else {
                logger.warning("⚠️ Received non-MapMessage: " + message.getClass().getName());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error processing reorder notification message", e);
        }
    }
    
    /**
     * Process reorder notification message
     */
    private void processReorderNotification(MapMessage message) throws JMSException {
        try {
            String alertType = message.getString("alertType");
            String source = message.getString("source");
            long timestamp = message.getLong("timestamp");
            logger.info("📧 Processing " + alertType + " notification from " + source +
                        " at " + new java.util.Date(timestamp));
            
            switch (alertType) {
                case "REORDER_REQUEST":
                    handleReorderRequest(message);
                    break;
                case "REORDER_APPROVED":
                    handleReorderApproval(message);
                    break;
                case "REORDER_REJECTED":
                    handleReorderRejection(message);
                    break;
                case "REORDER_COMPLETED":
                    handleReorderCompletion(message);
                    break;
                default:
                    logger.warning("⚠️ Unknown alert type: " + alertType);
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error processing reorder notification", e);
            throw new RuntimeException("Failed to process reorder notification", e);
        }
    }
    
    /**
     * Handle new reorder request notification
     */
    private void handleReorderRequest(MapMessage message) throws JMSException {
        try {
            int requestId = message.getInt("requestId");
            String componentName = message.getString("componentName");
            int requestedQuantity = message.getInt("requestedQuantity");
            String priority = message.getString("priority");
            
            // Prepare email content
            String subject = "🔔 New Reorder Request - " + componentName;
            String body = buildReorderRequestEmail(requestId, componentName, requestedQuantity, priority);
            
            // Send email to managers/approvers
            sendEmail("manager@semiconductor-inventory.com", subject, body);
            
            logger.info("📧 Sent reorder request notification for request ID: " + requestId);
            
            // Also send to procurement team if high priority
            if ("HIGH".equals(priority) || "URGENT".equals(priority)) {
                sendEmail("procurement@semiconductor-inventory.com", 
                         "🚨 URGENT: " + subject, body);
                logger.info("🚨 Sent urgent notification to procurement team");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error handling reorder request notification", e);
        }
    }
    
    /**
     * Handle reorder approval notification
     */
    private void handleReorderApproval(MapMessage message) throws JMSException {
        try {
            int requestId = message.getInt("requestId");
            String componentName = message.getString("componentName");
            String approvedBy = message.getString("approvedBy");
            
            String subject = "✅ Reorder Request Approved - " + componentName;
            String body = buildReorderApprovalEmail(requestId, componentName, approvedBy);
            
            // Send to procurement team
            sendEmail("procurement@semiconductor-inventory.com", subject, body);
            
            // Send to warehouse
            sendEmail("warehouse@semiconductor-inventory.com", subject, body);
            
            logger.info("📧 Sent reorder approval notifications for request ID: " + requestId);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error handling reorder approval notification", e);
        }
    }
    
    /**
     * Handle reorder rejection notification
     */
    private void handleReorderRejection(MapMessage message) throws JMSException {
        try {
            int requestId = message.getInt("requestId");
            String componentName = message.getString("componentName");
            String rejectedBy = message.getStringProperty("rejectedBy");
            String reason = message.getStringProperty("reason");
            
            String subject = "❌ Reorder Request Rejected - " + componentName;
            String body = buildReorderRejectionEmail(requestId, componentName, rejectedBy, reason);
            
            // Send to requester and managers
            sendEmail("manager@semiconductor-inventory.com", subject, body);
            
            logger.info("📧 Sent reorder rejection notification for request ID: " + requestId);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error handling reorder rejection notification", e);
        }
    }
    
    /**
     * Handle reorder completion notification
     */
    private void handleReorderCompletion(MapMessage message) throws JMSException {
        try {
            int requestId = message.getInt("requestId");
            String componentName = message.getString("componentName");
            int actualQuantity = message.getIntProperty("actualQuantity");
            
            String subject = "📦 Reorder Completed - " + componentName;
            String body = buildReorderCompletionEmail(requestId, componentName, actualQuantity);
            
            // Send to managers and warehouse
            sendEmail("manager@semiconductor-inventory.com", subject, body);
            sendEmail("warehouse@semiconductor-inventory.com", subject, body);
            
            logger.info("📧 Sent reorder completion notifications for request ID: " + requestId);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error handling reorder completion notification", e);
        }
    }
    
    /**
     * Send email notification
     */
    private void sendEmail(String toEmail, String subject, String body) {
        try {
            // Configure mail properties
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            
            // Create session
            Session session = Session.getInstance(props);
            
            // Create message
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(FROM_EMAIL));
            mimeMessage.addRecipient(RecipientType.TO, new InternetAddress(toEmail));
            mimeMessage.setSubject(subject);
            mimeMessage.setContent(body, "text/html; charset=utf-8");
            
            // In production, you would use Transport.send(message)
            // For now, just log the email content
            logger.info("📧 EMAIL WOULD BE SENT TO: " + toEmail);
            logger.info("📧 SUBJECT: " + subject);
            logger.info("📧 BODY: " + body);
            
            // Uncomment for actual email sending in production:
            // Transport.send(mimeMessage);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error sending email to: " + toEmail, e);
        }
    }
    
    /**
     * Build reorder request email template
     */
    private String buildReorderRequestEmail(int requestId, String componentName, int quantity, String priority) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #007bff;">🔔 New Reorder Request</h2>
                <div style="background-color: #f8f9fa; padding: 15px; border-left: 4px solid #007bff;">
                    <p><strong>Request ID:</strong> #%d</p>
                    <p><strong>Component:</strong> %s</p>
                    <p><strong>Requested Quantity:</strong> %d units</p>
                    <p><strong>Priority:</strong> <span style="color: %s; font-weight: bold;">%s</span></p>
                    <p><strong>Status:</strong> Pending Approval</p>
                </div>
                <p>Please review and approve this reorder request in the system.</p>
                <hr>
                <small style="color: #6c757d;">
                    Semiconductor Inventory Management System<br>
                    Automated notification - Do not reply to this email
                </small>
            </body>
            </html>
            """, requestId, componentName, quantity, 
            getPriorityColor(priority), priority);
    }
    
    /**
     * Build reorder approval email template
     */
    private String buildReorderApprovalEmail(int requestId, String componentName, String approvedBy) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #28a745;">✅ Reorder Request Approved</h2>
                <div style="background-color: #d4edda; padding: 15px; border-left: 4px solid #28a745;">
                    <p><strong>Request ID:</strong> #%d</p>
                    <p><strong>Component:</strong> %s</p>
                    <p><strong>Approved By:</strong> %s</p>
                    <p><strong>Status:</strong> Approved - Ready for Ordering</p>
                </div>
                <p>The reorder request has been approved. Please proceed with placing the order.</p>
                <hr>
                <small style="color: #6c757d;">
                    Semiconductor Inventory Management System<br>
                    Automated notification - Do not reply to this email
                </small>
            </body>
            </html>
            """, requestId, componentName, approvedBy);
    }
    
    /**
     * Build reorder rejection email template
     */
    private String buildReorderRejectionEmail(int requestId, String componentName, String rejectedBy, String reason) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #dc3545;">❌ Reorder Request Rejected</h2>
                <div style="background-color: #f8d7da; padding: 15px; border-left: 4px solid #dc3545;">
                    <p><strong>Request ID:</strong> #%d</p>
                    <p><strong>Component:</strong> %s</p>
                    <p><strong>Rejected By:</strong> %s</p>
                    <p><strong>Reason:</strong> %s</p>
                    <p><strong>Status:</strong> Rejected</p>
                </div>
                <p>The reorder request has been rejected. Please review the reason and take appropriate action.</p>
                <hr>
                <small style="color: #6c757d;">
                    Semiconductor Inventory Management System<br>
                    Automated notification - Do not reply to this email
                </small>
            </body>
            </html>
            """, requestId, componentName, rejectedBy, reason != null ? reason : "No reason provided");
    }
    
    /**
     * Build reorder completion email template
     */
    private String buildReorderCompletionEmail(int requestId, String componentName, int actualQuantity) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #17a2b8;">📦 Reorder Completed</h2>
                <div style="background-color: #d1ecf1; padding: 15px; border-left: 4px solid #17a2b8;">
                    <p><strong>Request ID:</strong> #%d</p>
                    <p><strong>Component:</strong> %s</p>
                    <p><strong>Received Quantity:</strong> %d units</p>
                    <p><strong>Status:</strong> Completed - Stock Updated</p>
                </div>
                <p>The reorder has been completed and stock levels have been updated in the system.</p>
                <hr>
                <small style="color: #6c757d;">
                    Semiconductor Inventory Management System<br>
                    Automated notification - Do not reply to this email
                </small>
            </body>
            </html>
            """, requestId, componentName, actualQuantity);
    }
    
    /**
     * Get priority color for email formatting
     */
    private String getPriorityColor(String priority) {
        return switch (priority.toUpperCase()) {
            case "HIGH", "URGENT" -> "#dc3545";
            case "MEDIUM" -> "#ffc107";
            case "LOW" -> "#28a745";
            default -> "#6c757d";
        };
    }
}