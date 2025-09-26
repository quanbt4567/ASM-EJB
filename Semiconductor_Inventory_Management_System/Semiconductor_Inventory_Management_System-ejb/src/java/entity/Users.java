/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * Enhanced Users Entity for Semiconductor Inventory Management System
 * @author MINH_QUAN - Updated on 2025-09-16
 */
@Entity
@Table(name = "users", catalog = "semiconductor_inventory_db", schema = "dbo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"email"}),
    @UniqueConstraint(columnNames = {"username"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Users.findAll", query = "SELECT u FROM Users u"),
    @NamedQuery(name = "Users.findActive", query = "SELECT u FROM Users u WHERE u.active = true ORDER BY u.fullName"),
    @NamedQuery(name = "Users.findByUserId", query = "SELECT u FROM Users u WHERE u.userId = :userId"),
    @NamedQuery(name = "Users.findByUsername", query = "SELECT u FROM Users u WHERE u.username = :username"),
    @NamedQuery(name = "Users.findByUsernameAndActive", query = "SELECT u FROM Users u WHERE u.username = :username AND u.active = true"),
    @NamedQuery(name = "Users.findByEmail", query = "SELECT u FROM Users u WHERE u.email = :email"),
    @NamedQuery(name = "Users.findByEmailAndActive", query = "SELECT u FROM Users u WHERE u.email = :email AND u.active = true"),
    @NamedQuery(name = "Users.findByPasswordHash", query = "SELECT u FROM Users u WHERE u.passwordHash = :passwordHash"),
    @NamedQuery(name = "Users.findByFullName", query = "SELECT u FROM Users u WHERE u.fullName = :fullName"),
    @NamedQuery(name = "Users.findByRole", query = "SELECT u FROM Users u WHERE u.role = :role AND u.active = true ORDER BY u.fullName"),
    @NamedQuery(name = "Users.findByActive", query = "SELECT u FROM Users u WHERE u.active = :active"),
    @NamedQuery(name = "Users.findByCreatedAt", query = "SELECT u FROM Users u WHERE u.createdAt = :createdAt"),
    @NamedQuery(name = "Users.findByLastLogin", query = "SELECT u FROM Users u WHERE u.lastLogin = :lastLogin"),
    @NamedQuery(name = "Users.findByDepartment", query = "SELECT u FROM Users u WHERE u.department = :department AND u.active = true ORDER BY u.fullName"),
    @NamedQuery(name = "Users.findByPhone", query = "SELECT u FROM Users u WHERE u.phone = :phone"),
    @NamedQuery(name = "Users.searchUsers", query = "SELECT u FROM Users u WHERE u.active = true AND (LOWER(u.username) LIKE LOWER(:searchTerm) OR LOWER(u.fullName) LIKE LOWER(:searchTerm) OR LOWER(u.email) LIKE LOWER(:searchTerm)) ORDER BY u.fullName"),
    @NamedQuery(name = "Users.countByRole", query = "SELECT COUNT(u) FROM Users u WHERE u.role = :role AND u.active = true"),
    @NamedQuery(name = "Users.countActiveUsers", query = "SELECT COUNT(u) FROM Users u WHERE u.active = true")})
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Basic(optional = false)
    @NotNull(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    @Basic(optional = false)
    @NotNull(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(min = 1, max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Basic(optional = false)
    @NotNull(message = "Password is required")
    @Size(min = 1, max = 255, message = "Password hash must not exceed 255 characters")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Basic(optional = false)
    @NotNull(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Basic(optional = false)
    @NotNull(message = "Role is required")
    @Pattern(regexp = "^(ADMIN|MANAGER|USER|VIEWER)$", message = "Role must be ADMIN, MANAGER, USER, or VIEWER")
    @Column(name = "role", nullable = false, length = 20)
    private String role;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "active", nullable = false)
    private boolean active;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    @Column(name = "last_login")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;
    
    @Size(max = 50, message = "Department name must not exceed 50 characters")
    @Column(name = "department", length = 50)
    private String department;
    
    @Pattern(regexp = "^$|^[+]?[0-9\\s\\-\\(\\)]{10,20}$", message = "Please provide a valid phone number")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;
    
    // Relationship mappings
    @OneToMany(mappedBy = "createdBy")
    private Collection<Components> componentsCollection;
    
    @OneToMany(mappedBy = "processedBy")
    private Collection<StockAlerts> stockAlertsCollection;
    
    @OneToMany(mappedBy = "approvedBy")
    private Collection<ReorderRequests> reorderRequestsCollection;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Collection<Transactions> transactionsCollection;

    // =============== CONSTRUCTORS ===============
    
    public Users() {
        this.active = true;
    }

    public Users(Integer userId) {
        this();
        this.userId = userId;
    }

    public Users(String username, String email, String passwordHash, String fullName, String role) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    public Users(Integer userId, String username, String email, String passwordHash, String fullName, String role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    // =============== LIFECYCLE CALLBACKS ===============
    
    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.role == null) {
            this.role = "USER"; // Default role
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }

    // =============== GETTERS AND SETTERS ===============

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @XmlTransient
    public Collection<Components> getComponentsCollection() {
        return componentsCollection;
    }

    public void setComponentsCollection(Collection<Components> componentsCollection) {
        this.componentsCollection = componentsCollection;
    }

    @XmlTransient
    public Collection<StockAlerts> getStockAlertsCollection() {
        return stockAlertsCollection;
    }

    public void setStockAlertsCollection(Collection<StockAlerts> stockAlertsCollection) {
        this.stockAlertsCollection = stockAlertsCollection;
    }

    @XmlTransient
    public Collection<ReorderRequests> getReorderRequestsCollection() {
        return reorderRequestsCollection;
    }

    public void setReorderRequestsCollection(Collection<ReorderRequests> reorderRequestsCollection) {
        this.reorderRequestsCollection = reorderRequestsCollection;
    }

    @XmlTransient
    public Collection<Transactions> getTransactionsCollection() {
        return transactionsCollection;
    }

    public void setTransactionsCollection(Collection<Transactions> transactionsCollection) {
        this.transactionsCollection = transactionsCollection;
    }

    // =============== BUSINESS METHODS ===============
    
    /**
     * Check if user is an admin
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if user is a manager
     */
    public boolean isManager() {
        return "MANAGER".equalsIgnoreCase(this.role);
    }
    
    /**
     * Check if user can edit components
     */
    public boolean canEditComponents() {
        return isAdmin() || isManager();
    }
    
    /**
     * Check if user can view reports
     */
    public boolean canViewReports() {
        return isAdmin() || isManager() || "USER".equalsIgnoreCase(this.role);
    }
    
    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return fullName != null && !fullName.trim().isEmpty() ? fullName : username;
    }
    
    /**
     * Get role display name
     */
    public String getRoleDisplayName() {
        switch (role.toUpperCase()) {
            case "ADMIN": return "Administrator";
            case "MANAGER": return "Manager";
            case "USER": return "User";
            case "VIEWER": return "Viewer";
            default: return role;
        }
    }
    
    /**
     * Check if user has been active recently (within last 30 days)
     */
    public boolean isRecentlyActive() {
        if (lastLogin == null) return false;
        
        long daysSinceLogin = (new Date().getTime() - lastLogin.getTime()) / (1000 * 60 * 60 * 24);
        return daysSinceLogin <= 30;
    }
    
    /**
     * Get initials for avatar display
     */
    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return username.substring(0, Math.min(2, username.length())).toUpperCase();
        }
        
        String[] nameParts = fullName.trim().split("\\s+");
        if (nameParts.length >= 2) {
            return (nameParts[0].substring(0, 1) + nameParts[nameParts.length - 1].substring(0, 1)).toUpperCase();
        } else {
            return nameParts[0].substring(0, Math.min(2, nameParts[0].length())).toUpperCase();
        }
    }

    // =============== OVERRIDE METHODS ===============

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Users)) {
            return false;
        }
        Users other = (Users) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Users{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", department='" + department + '\'' +
                '}';
    }
}