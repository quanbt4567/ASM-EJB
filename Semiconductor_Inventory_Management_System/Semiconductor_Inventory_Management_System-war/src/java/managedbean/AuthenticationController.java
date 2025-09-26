/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package managedbean;

import ejb.UserEJB;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuthenticationController - Handles user authentication and session management
 * 
 * @author MINH_QUAN - Created on 2025-09-16
 */
@Named("authController")
@SessionScoped
public class AuthenticationController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AuthenticationController.class.getName());
    
    @EJB
    private UserEJB userEJB;
    
    // Login form fields
    private String username;
    private String password;
    private boolean rememberMe;
    
    // Current user session
    private Users currentUser;
    private boolean loggedIn = false;
    
    @PostConstruct
    public void init() {
        logger.info("🔐 AuthenticationController initialized");
    }
    
    /**
     * Handle user login
     */
    public String login() {
        try {
            logger.info("🔐 Login attempt for username: " + username);
            
            if (username == null || username.trim().isEmpty()) {
                addErrorMessage("Username is required");
                return null;
            }
            
            if (password == null || password.trim().isEmpty()) {
                addErrorMessage("Password is required");
                return null;
            }
            
            // Hash the password
            String hashedPassword = hashPassword(password);
            
            // Authenticate user
            Users user = userEJB.authenticateUser(username.trim(), hashedPassword);
            
            if (user != null && user.getActive()) {
                // Login successful
                currentUser = user;
                loggedIn = true;
                
                // Clear form
                username = null;
                password = null;
                
                // Update last login
                userEJB.updateLastLogin(user.getUserId());
                
                logger.info("✅ User logged in successfully: " + user.getUsername());
                addInfoMessage("Welcome, " + user.getFullName() + "!");
                
                // Redirect to dashboard
                return "/pages/index.xhtml?faces-redirect=true";
                
            } else {
                // Login failed
                addErrorMessage("Invalid username or password");
                logger.warning("❌ Failed login attempt for username: " + username);
                return null;
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error during login", e);
            addErrorMessage("Login failed. Please try again.");
            return null;
        }
    }
    
    /**
     * Handle user logout
     */
    public String logout() {
        try {
            if (currentUser != null) {
                logger.info("🔐 User logging out: " + currentUser.getUsername());
            }
            
            // Clear session
            currentUser = null;
            loggedIn = false;
            username = null;
            password = null;
            
            // Invalidate HTTP session
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.invalidateSession();
            
            addInfoMessage("You have been logged out successfully");
            
            // Redirect to login page
            return "/pages/login.xhtml?faces-redirect=true";
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error during logout", e);
            addErrorMessage("Logout failed");
            return null;
        }
    }
    
    /**
     * Check if user is logged in and redirect if not
     */
    public void checkAuthentication() {
        if (!loggedIn || currentUser == null) {
            try {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                ExternalContext externalContext = facesContext.getExternalContext();
                externalContext.redirect(externalContext.getRequestContextPath() + "/pages/login.xhtml");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "❌ Error redirecting to login", e);
            }
        }
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(String role) {
        return currentUser != null && role.equals(currentUser.getRole());
    }
    
    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Check if user is manager
     */
    public boolean isManager() {
        return hasRole("MANAGER") || isAdmin();
    }
    
    /**
     * Check if user is employee
     */
    public boolean isEmployee() {
        return hasRole("EMPLOYEE") || isManager();
    }
    
    /**
     * Get current user's display name
     */
    public String getCurrentUserDisplayName() {
        if (currentUser != null) {
            return currentUser.getFullName() != null ? currentUser.getFullName() : currentUser.getUsername();
        }
        return "Guest";
    }
    
    /**
     * Get current user's initials for avatar
     */
    public String getCurrentUserInitials() {
        if (currentUser != null && currentUser.getFullName() != null) {
            String[] nameParts = currentUser.getFullName().split(" ");
            StringBuilder initials = new StringBuilder();
            for (String part : nameParts) {
                if (!part.isEmpty()) {
                    initials.append(part.charAt(0));
                }
            }
            return initials.toString().toUpperCase();
        }
        return "U";
    }
    
    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "❌ Error hashing password", e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    /**
     * Add error message to faces context
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }
    
    /**
     * Add info message to faces context
     */
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", message));
    }
    
    
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isRememberMe() {
        return rememberMe;
    }
    
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    public Users getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(Users currentUser) {
        this.currentUser = currentUser;
        this.loggedIn = (currentUser != null);
    }
    
    public boolean isLoggedIn() {
        return loggedIn && currentUser != null;
    }
    
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}