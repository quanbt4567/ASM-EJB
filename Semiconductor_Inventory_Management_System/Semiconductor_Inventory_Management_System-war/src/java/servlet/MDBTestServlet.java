package servlet;

import ejb.ComponentEJB;
import ejb.NotificationService;
import ejb.TransactionEJB;
import entity.Components;
import entity.StockAlerts;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "MDBTestServlet", urlPatterns = {"/test-mdb"})
public class MDBTestServlet extends HttpServlet {
    
    @EJB
    private ComponentEJB componentEJB;
    
    @EJB
    private TransactionEJB transactionEJB;
    
    @EJB
    private NotificationService notificationService;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>MDB Test Results - Java Ant Project</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
            out.println(".container { max-width: 1000px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
            out.println(".success { color: green; }");
            out.println(".error { color: red; }");
            out.println(".warning { color: orange; }");
            out.println(".info { color: blue; }");
            out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
            out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            out.println("th { background-color: #f2f2f2; }");
            out.println(".btn { background: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin: 5px; display: inline-block; }");
            out.println(".btn:hover { background: #0056b3; }");
            out.println(".btn-danger { background: #dc3545; } .btn-danger:hover { background: #c82333; }");
            out.println(".btn-warning { background: #ffc107; color: black; } .btn-warning:hover { background: #e0a800; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            
            out.println("<div class='container'>");
            out.println("<h1>🧪 MDB Test Results - Java Ant Project</h1>");
            out.println("<p>Testing Message-Driven Bean functionality for stock alerts in Ant-based EJB application</p>");
            
            // Test 1: Current stock status
            out.println("<h2>📊 Current Stock Status</h2>");
            displayCurrentStockStatus(out);
            
            // Test 2: Trigger alerts
            String action = request.getParameter("action");
            if ("trigger-alerts".equals(action)) {
                out.println("<h2>🔥 Triggering Stock Alerts</h2>");
                triggerStockAlerts(out);
            } else if ("trigger-critical".equals(action)) {
                out.println("<h2>🚨 Triggering Critical Alerts</h2>");
                triggerCriticalAlerts(out);
            }
            
            // Test 3: Show recent alerts
            out.println("<h2>🔔 Recent Stock Alerts (Last 10)</h2>");
            displayRecentAlerts(out);
            
            // Test controls
            out.println("<h2>🎮 Test Controls</h2>");
            out.println("<a href='test-mdb?action=trigger-alerts' class='btn'>🚨 Trigger Normal Alerts</a>");
            out.println("<a href='test-mdb?action=trigger-critical' class='btn btn-danger'>🔥 Trigger Critical Alerts</a>");
            out.println("<a href='test-mdb' class='btn btn-warning'>🔄 Refresh Page</a>");
            
            // Instructions
            out.println("<h2>📋 Test Instructions</h2>");
            out.println("<ol>");
            out.println("<li>Click 'Trigger Normal Alerts' to test LOW_STOCK alerts</li>");
            out.println("<li>Click 'Trigger Critical Alerts' to test OUT_OF_STOCK alerts</li>");
            out.println("<li>Monitor server logs for MDB processing messages</li>");
            out.println("<li>Check database for new stock_alerts records</li>");
            out.println("<li>Wait a few seconds between triggers for MDB processing</li>");
            out.println("</ol>");
            
            out.println("<div style='background: #e3f2fd; padding: 15px; border-radius: 5px; margin-top: 20px;'>");
            out.println("<h3>🏗️ Java Ant Project Structure:</h3>");
            out.println("<p><strong>Source:</strong> src/java/</p>");
            out.println("<p><strong>Web:</strong> web/</p>");
            out.println("<p><strong>Build:</strong> build/</p>");
            out.println("<p><strong>Deploy:</strong> GlassFish Server</p>");
            out.println("</div>");
            
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
            
        } catch (Exception e) {
            out.println("<p class='error'>❌ Error: " + e.getMessage() + "</p>");
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
    
    private void displayCurrentStockStatus(PrintWriter out) {
        try {
            List<Components> lowStockComponents = componentEJB.getLowStockComponents();
            List<Components> outOfStockComponents = componentEJB.getOutOfStockComponents();
            
            out.println("<table>");
            out.println("<tr><th>Component</th><th>Current Stock</th><th>Reorder Level</th><th>Status</th><th>Actions</th></tr>");
            
            // Show out of stock components
            for (Components comp : outOfStockComponents) {
                out.println("<tr style='background-color: #ffebee;'>");
                out.println("<td>" + comp.getName() + "</td>");
                out.println("<td class='error'>" + comp.getQuantity() + "</td>");
                out.println("<td>" + comp.getReorderLevel() + "</td>");
                out.println("<td class='error'>🔴 OUT OF STOCK</td>");
                out.println("<td><a href='test-mdb?action=restock&id=" + comp.getComponentId() + "' class='btn' style='padding: 5px 10px; font-size: 12px;'>➕ Restock</a></td>");
                out.println("</tr>");
            }
            
            // Show low stock components
            for (Components comp : lowStockComponents) {
                if (comp.getQuantity() > 0) { // Don't show out of stock again
                    out.println("<tr style='background-color: #fff3e0;'>");
                    out.println("<td>" + comp.getName() + "</td>");
                    out.println("<td class='warning'>" + comp.getQuantity() + "</td>");
                    out.println("<td>" + comp.getReorderLevel() + "</td>");
                    out.println("<td class='warning'>⚠️ LOW STOCK</td>");
                    out.println("<td><a href='test-mdb?action=consume&id=" + comp.getComponentId() + "' class='btn btn-warning' style='padding: 5px 10px; font-size: 12px;'>➖ Consume</a></td>");
                    out.println("</tr>");
                }
            }
            
            // Show some healthy stock components for testing
            List<Components> allComponents = componentEJB.getAllComponents();
            int healthyCount = 0;
            for (Components comp : allComponents) {
                if (comp.getQuantity() > comp.getReorderLevel() && healthyCount < 3) {
                    out.println("<tr style='background-color: #e8f5e8;'>");
                    out.println("<td>" + comp.getName() + "</td>");
                    out.println("<td class='success'>" + comp.getQuantity() + "</td>");
                    out.println("<td>" + comp.getReorderLevel() + "</td>");
                    out.println("<td class='success'>✅ HEALTHY</td>");
                    out.println("<td><a href='test-mdb?action=test-consume&id=" + comp.getComponentId() + "' class='btn' style='padding: 5px 10px; font-size: 12px;'>🧪 Test Consume</a></td>");
                    out.println("</tr>");
                    healthyCount++;
                }
            }
            
            out.println("</table>");
            
            if (outOfStockComponents.isEmpty() && lowStockComponents.isEmpty()) {
                out.println("<p class='success'>✅ Most components have healthy stock levels!</p>");
            }
            
        } catch (Exception e) {
            out.println("<p class='error'>❌ Error displaying stock status: " + e.getMessage() + "</p>");
        }
    }
    
    private void triggerStockAlerts(PrintWriter out) {
        try {
            out.println("<p class='info'>🔥 Triggering stock consumption to test MDB...</p>");
            
            // Test 1: Trigger LOW_STOCK for a healthy component
            List<Components> healthyComponents = componentEJB.getAllComponents();
            for (Components comp : healthyComponents) {
                if (comp.getQuantity() > comp.getReorderLevel() + 10) {
                    int consumeAmount = comp.getQuantity() - comp.getReorderLevel() + 5;
                    transactionEJB.recordExportTransaction(comp.getComponentId(), consumeAmount, "Ant MDB Test - LOW_STOCK trigger");
                    out.println("<p class='warning'>⚠️ Triggered LOW_STOCK alert for " + comp.getName() + "</p>");
                    break;
                }
            }
            
            // Test 2: Direct notification service test
            notificationService.sendCustomAlert(1, "CRITICAL_STOCK", "Ant MDB Test - Direct notification", 2, 10);
            out.println("<p class='info'>📤 Sent custom alert via NotificationService</p>");
            
            out.println("<p class='success'>✅ Alert triggers sent! MDB should process them shortly.</p>");
            out.println("<p><em>⏳ Wait a few seconds and refresh to see new alerts...</em></p>");
            
        } catch (Exception e) {
            out.println("<p class='error'>❌ Error triggering alerts: " + e.getMessage() + "</p>");
        }
    }
    
    private void triggerCriticalAlerts(PrintWriter out) {
        try {
            out.println("<p class='error'>🚨 Triggering CRITICAL stock alerts...</p>");
            
            // Find component with some stock and drain it completely
            List<Components> components = componentEJB.getAllComponents();
            for (Components comp : components) {
                if (comp.getQuantity() > 0 && comp.getQuantity() <= 20) { // Small stock components
                    transactionEJB.recordExportTransaction(comp.getComponentId(), comp.getQuantity(), "Ant MDB Test - OUT_OF_STOCK trigger");
                    out.println("<p class='error'>🔴 Triggered OUT_OF_STOCK alert for " + comp.getName() + "</p>");
                    break;
                }
            }
            
            out.println("<p class='error'>🚨 CRITICAL alerts triggered! Check MDB logs.</p>");
            
        } catch (Exception e) {
            out.println("<p class='error'>❌ Error triggering critical alerts: " + e.getMessage() + "</p>");
        }
    }
    
    private void displayRecentAlerts(PrintWriter out) {
        try {
            List<StockAlerts> alerts = componentEJB.getRecentStockAlerts(10);
            
            if (alerts.isEmpty()) {
                out.println("<p class='info'>ℹ️ No alerts found. Try triggering some alerts first.</p>");
                return;
            }
            
            out.println("<table>");
            out.println("<tr><th>Alert ID</th><th>Component</th><th>Type</th><th>Severity</th><th>Description</th><th>Created</th><th>Processed</th></tr>");
            
            for (StockAlerts alert : alerts) {
                String rowStyle = "";
                switch (alert.getSeverity()) {
                    case "CRITICAL": rowStyle = "background-color: #ffebee;"; break;
                    case "HIGH": rowStyle = "background-color: #fff3e0;"; break;
                    case "MEDIUM": rowStyle = "background-color: #f3e5f5;"; break;
                    default: rowStyle = "background-color: #e8f5e8;"; break;
                }
                
                out.println("<tr style='" + rowStyle + "'>");
                out.println("<td>" + alert.getAlertId() + "</td>");
                out.println("<td>" + (alert.getComponentId() != null ? alert.getComponentId().getName() : "Unknown") + "</td>");
                out.println("<td>" + alert.getAlertType() + "</td>");
                out.println("<td>" + alert.getSeverity() + "</td>");
                out.println("<td>" + (alert.getDescription() != null ? 
                           (alert.getDescription().length() > 50 ? 
                            alert.getDescription().substring(0, 50) + "..." : 
                            alert.getDescription()) : "") + "</td>");
                out.println("<td>" + alert.getCreatedAt() + "</td>");
                out.println("<td>" + (alert.getProcessed() ? "✅ Yes" : "⏳ No") + "</td>");
                out.println("</tr>");
            }
            
            out.println("</table>");
            
        } catch (Exception e) {
            out.println("<p class='error'>❌ Error displaying alerts: " + e.getMessage() + "</p>");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}