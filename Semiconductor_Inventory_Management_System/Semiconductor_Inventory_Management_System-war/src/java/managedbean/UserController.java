/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package managedbean;

import ejb.UserEJB;
import entity.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UserController - Manages user CRUD operations and user management
 * 
 * @author MINH_QUAN - Created on 2025-09-16
 */
@Named("userController")
@RequestScoped
public class UserController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    
    @EJB
    private UserEJB userEJB;
    
    @Inject
    private AuthenticationController authController;
    
    // Form fields for user management
    private Users selectedUser;
    private Users newUser;
    private List<Users> userList;
    private String searchTerm;
    private String selectedRole;
    private boolean showInactive = false;
    
    // Password fields
    private String newPassword;
    private String confirmPassword;
    private String currentPassword;
    
    @PostConstruct
    public void init() {
        logger.info("👤 UserController initialized");
        newUser = new Users();
        loadUsers();
    }
    
    /**
     * Load all users
     */
    public void loadUsers() {
        try {
            if (showInactive) {
                userList = userEJB.findAllUsers();
            } else {
                userList = userEJB.findActiveUsers();
            }
            logger.info("📋 Loaded " + userList.size() + " users");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error loading users", e);
            addErrorMessage("Failed to load users");
        }
    }
    
    /**
     * Search users
     */
    public void searchUsers() {
        try {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                userList = userEJB.searchUsers(searchTerm.trim());
                logger.info("🔍 Found " + userList.size() + " users matching: " + searchTerm);
            } else {
                loadUsers();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error searching users", e);
            addErrorMessage("Search failed");
        }
    }
    
    /**
     * Filter users by role
     */
    public void filterByRole() {
        try {
            if (selectedRole != null && !selectedRole.trim().isEmpty()) {
                userList = userEJB.findUsersByRole(selectedRole);
                logger.info("🔍 Found " + userList.size() + " users with role: " + selectedRole);
            } else {
                loadUsers();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error filtering users by role", e);
            addErrorMessage("Filter failed");
        }
    }
    
    /**
     * Create new user
     */
    public void createUser() {
        try {
            if (!validateUserForm()) {
                return;
            }
            
            if (!validatePassword()) {
                return;
            }
            
            // Check if username already exists
            if (userEJB.findByUsername(newUser.getUsername()) != null) {
                addErrorMessage("Username already exists");
                return;
            }
            
            // Check if email already exists
            if (userEJB.findByEmail(newUser.getEmail()) != null) {
                addErrorMessage("Email already exists");
                return;
            }
            
            // Set additional fields
            newUser.setPasswordHash(hashPassword(newPassword));
            newUser.setActive(true);
            newUser.setCreatedAt(new Date());
            
            // Create user
            userEJB.createUser(newUser);
            
            logger.info("✅ Created new user: " + newUser.getUsername());
            addInfoMessage("User created successfully");
            
            // Reset form and reload
            newUser = new Users();
            newPassword = null;
            confirmPassword = null;
            loadUsers();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error creating user", e);
            addErrorMessage("Failed to create user");
        }
    }
    
    /**
     * Update existing user
     */
    public void updateUser() {
        try {
            if (selectedUser == null) {
                addErrorMessage("No user selected");
                return;
            }
            
            if (!validateUserForm(selectedUser)) {
                return;
            }
            
            selectedUser.setUpdatedAt(new Date());
            userEJB.updateUser(selectedUser);
            
            logger.info("✅ Updated user: " + selectedUser.getUsername());
            addInfoMessage("User updated successfully");
            
            loadUsers();
            selectedUser = null;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error updating user", e);
            addErrorMessage("Failed to update user");
        }
    }
    
    /**
     * Delete user (deactivate)
     */
    public void deleteUser(Users user) {
        try {
            if (user == null) {
                addErrorMessage("No user selected");
                return;
            }
            
            if (user.getUserId().equals(authController.getCurrentUser().getUserId())) {
                addErrorMessage("Cannot delete your own account");
                return;
            }
            
            userEJB.deactivateUser(user.getUserId());
            
            logger.info("🗑️ Deactivated user: " + user.getUsername());
            addInfoMessage("User deactivated successfully");
            
            loadUsers();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error deactivating user", e);
            addErrorMessage("Failed to deactivate user");
        }
    }
    
    /**
     * Activate user
     */
    public void activateUser(Users user) {
        try {
            if (user == null) {
                addErrorMessage("No user selected");
                return;
            }
            
            userEJB.activateUser(user.getUserId());
            
            logger.info("✅ Activated user: " + user.getUsername());
            addInfoMessage("User activated successfully");
            
            loadUsers();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error activating user", e);
            addErrorMessage("Failed to activate user");
        }
    }
    
    /**
     * Reset user password
     */
    public void resetPassword() {
        try {
            if (selectedUser == null) {
                addErrorMessage("No user selected");
                return;
            }
            
            if (!validatePassword()) {
                return;
            }
            
            selectedUser.setPasswordHash(hashPassword(newPassword));
            selectedUser.setUpdatedAt(new Date());
            userEJB.updateUser(selectedUser);
            
            logger.info("🔑 Reset password for user: " + selectedUser.getUsername());
            addInfoMessage("Password reset successfully");
            
            newPassword = null;
            confirmPassword = null;
            selectedUser = null;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error resetting password", e);
            addErrorMessage("Failed to reset password");
        }
    }
    
    /**
     * Change current user's password
     */
    public void changePassword() {
        try {
            Users currentUser = authController.getCurrentUser();
            if (currentUser == null) {
                addErrorMessage("Not logged in");
                return;
            }
            
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                addErrorMessage("Current password is required");
                return;
            }
            
            if (!validatePassword()) {
                return;
            }
            
            // Verify current password
            String hashedCurrentPassword = hashPassword(currentPassword);
            if (!hashedCurrentPassword.equals(currentUser.getPasswordHash())) {
                addErrorMessage("Current password is incorrect");
                return;
            }
            
            // Update password
            currentUser.setPasswordHash(hashPassword(newPassword));
            currentUser.setUpdatedAt(new Date());
            userEJB.updateUser(currentUser);
            
            logger.info("🔑 User changed their password: " + currentUser.getUsername());
            addInfoMessage("Password changed successfully");
            
            // Clear form
            currentPassword = null;
            newPassword = null;
            confirmPassword = null;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Error changing password", e);
            addErrorMessage("Failed to change password");
        }
    }
    
    /**
     * Validate user form
     */
    private boolean validateUserForm() {
        return validateUserForm(newUser);
    }
    
    private boolean validateUserForm(Users user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            addErrorMessage("Username is required");
            return false;
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            addErrorMessage("Email is required");
            return false;
        }
        
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            addErrorMessage("Full name is required");
            return false;
        }
        
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            addErrorMessage("Role is required");
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate password
     */
    private boolean validatePassword() {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            addErrorMessage("Password is required");
            return false;
        }
        
        if (newPassword.length() < 6) {
            addErrorMessage("Password must be at least 6 characters");
            return false;
        }
        
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            addErrorMessage("Passwords do not match");
            return false;
        }
        
        return true;
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
     * Get user role options
     */
    public String[] getRoleOptions() {
        return new String[]{"ADMIN", "MANAGER", "EMPLOYEE"};
    }
    
    /**
     * Get user status badge class
     */
    public String getUserStatusClass(Users user) {
        return user.getActive() ? "badge bg-success" : "badge bg-secondary";
    }
    
    /**
     * Get user status text
     */
    public String getUserStatusText(Users user) {
        return user.getActive() ? "Active" : "Inactive";
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
    public Users getSelectedUser() {
        return selectedUser;
    }
    
    public void setSelectedUser(Users selectedUser) {
        this.selectedUser = selectedUser;
    }
    
    public Users getNewUser() {
        return newUser;
    }
    
    public void setNewUser(Users newUser) {
        this.newUser = newUser;
    }
    
    public List<Users> getUserList() {
        return userList;
    }
    
    public void setUserList(List<Users> userList) {
        this.userList = userList;
    }
    
    public String getSearchTerm() {
        return searchTerm;
    }
    
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
    
    public String getSelectedRole() {
        return selectedRole;
    }
    
    public void setSelectedRole(String selectedRole) {
        this.selectedRole = selectedRole;
    }
    
    public boolean isShowInactive() {
        return showInactive;
    }
    
    public void setShowInactive(boolean showInactive) {
        this.showInactive = showInactive;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    public String getCurrentPassword() {
        return currentPassword;
    }
    
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
}