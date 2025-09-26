package ejb;

import entity.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class UserEJB {
    
    private static final Logger logger = Logger.getLogger(UserEJB.class.getName());
    
    @PersistenceContext(unitName = "semiconductor-pu")
    private EntityManager em;
    
    // =============== AUTHENTICATION ===============
    
    public Users authenticateUser(String username, String password) {
        try {
            String hashedPassword = hashPassword(password);
            TypedQuery<Users> query = em.createQuery(
                "SELECT u FROM Users u WHERE u.username = :username AND u.passwordHash = :password AND u.active = true",
                Users.class);
            query.setParameter("username", username);
            query.setParameter("password", hashedPassword);
            
            List<Users> results = query.getResultList();
            if (!results.isEmpty()) {
                Users user = results.get(0);
                user.setLastLogin(new Date());
                em.merge(user);
                logger.info("User authenticated: " + username);
                return user;
            }
            return null;
        } catch (Exception e) {
            logger.severe("Error authenticating user: " + e.getMessage());
            return null;
        }
    }
    
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        try {
            Users user = getUserById(userId);
            if (user != null && user.getPasswordHash().equals(hashPassword(oldPassword))) {
                user.setPasswordHash(hashPassword(newPassword));
                user.setUpdatedAt(new Date());
                em.merge(user);
                logger.info("Password changed for user: " + user.getUsername());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.severe("Error changing password: " + e.getMessage());
            return false;
        }
    }
    
    // =============== CRUD OPERATIONS ===============
    
    public void createUser(Users user) {
        try {
            user.setPasswordHash(hashPassword(user.getPasswordHash())); // Hash the password
            user.setActive(true);
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            em.persist(user);
            logger.info("Created user: " + user.getUsername());
        } catch (Exception e) {
            logger.severe("Error creating user: " + e.getMessage());
            throw new RuntimeException("Failed to create user", e);
        }
    }
    
    public Users getUserById(Integer userId) {
        try {
            return em.find(Users.class, userId);
        } catch (Exception e) {
            logger.severe("Error finding user by ID: " + e.getMessage());
            throw new RuntimeException("Failed to find user", e);
        }
    }
    
    public Users getUserByUsername(String username) {
        try {
            TypedQuery<Users> query = em.createQuery(
                "SELECT u FROM Users u WHERE u.username = :username", Users.class);
            query.setParameter("username", username);
            List<Users> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logger.severe("Error finding user by username: " + e.getMessage());
            throw new RuntimeException("Failed to find user by username", e);
        }
    }
    
    public void updateUser(Users user) {
        try {
            user.setUpdatedAt(new Date());
            em.merge(user);
            logger.info("Updated user: " + user.getUsername());
        } catch (Exception e) {
            logger.severe("Error updating user: " + e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }
    
    public void deleteUser(Integer userId) {
        try {
            Users user = getUserById(userId);
            if (user != null) {
                user.setActive(false); // Soft delete
                user.setUpdatedAt(new Date());
                em.merge(user);
                logger.info("Soft deleted user: " + user.getUsername());
            }
        } catch (Exception e) {
            logger.severe("Error deleting user: " + e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }
    
    // =============== QUERY OPERATIONS ===============
    
    public List<Users> getAllUsers() {
        try {
            TypedQuery<Users> query = em.createQuery(
                "SELECT u FROM Users u WHERE u.active = true ORDER BY u.username", Users.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error getting all users: " + e.getMessage());
            throw new RuntimeException("Failed to get users", e);
        }
    }
    
    public List<Users> getUsersByRole(String role) {
        try {
            TypedQuery<Users> query = em.createQuery(
                "SELECT u FROM Users u WHERE u.active = true AND u.role = :role ORDER BY u.username", 
                Users.class);
            query.setParameter("role", role);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error getting users by role: " + e.getMessage());
            throw new RuntimeException("Failed to get users by role", e);
        }
    }
    
    public List<Users> searchUsers(String searchTerm) {
        try {
            TypedQuery<Users> query = em.createQuery(
                "SELECT u FROM Users u WHERE u.active = true AND " +
                "(LOWER(u.username) LIKE LOWER(:searchTerm) OR " +
                "LOWER(u.fullName) LIKE LOWER(:searchTerm) OR " +
                "LOWER(u.email) LIKE LOWER(:searchTerm)) " +
                "ORDER BY u.username", Users.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error searching users: " + e.getMessage());
            throw new RuntimeException("Failed to search users", e);
        }
    }
    
    // =============== VALIDATION ===============
    
    public boolean isUsernameUnique(String username, Integer excludeUserId) {
        try {
            TypedQuery<Long> query;
            if (excludeUserId != null) {
                query = em.createQuery(
                    "SELECT COUNT(u) FROM Users u WHERE u.username = :username AND u.userId != :excludeId", 
                    Long.class);
                query.setParameter("excludeId", excludeUserId);
            } else {
                query = em.createQuery(
                    "SELECT COUNT(u) FROM Users u WHERE u.username = :username", Long.class);
            }
            query.setParameter("username", username);
            return query.getSingleResult() == 0;
        } catch (Exception e) {
            logger.severe("Error checking username uniqueness: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isEmailUnique(String email, Integer excludeUserId) {
        try {
            TypedQuery<Long> query;
            if (excludeUserId != null) {
                query = em.createQuery(
                    "SELECT COUNT(u) FROM Users u WHERE u.email = :email AND u.userId != :excludeId", 
                    Long.class);
                query.setParameter("excludeId", excludeUserId);
            } else {
                query = em.createQuery(
                    "SELECT COUNT(u) FROM Users u WHERE u.email = :email", Long.class);
            }
            query.setParameter("email", email);
            return query.getSingleResult() == 0;
        } catch (Exception e) {
            logger.severe("Error checking email uniqueness: " + e.getMessage());
            return false;
        }
    }
    
    // =============== STATISTICS ===============
    
    public List<Object[]> getUserRoleStatistics() {
        try {
            TypedQuery<Object[]> query = em.createQuery(
                "SELECT u.role, COUNT(u) FROM Users u WHERE u.active = true " +
                "GROUP BY u.role ORDER BY COUNT(u) DESC", Object[].class);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error getting user role statistics: " + e.getMessage());
            throw new RuntimeException("Failed to get user role statistics", e);
        }
    }
    
    // =============== UTILITY METHODS ===============
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            logger.severe("Error hashing password: " + e.getMessage());
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    public void updateLastLogin(Integer userId) {
        try {
            Users user = getUserById(userId);
            if (user != null) {
                user.setLastLogin(new Date());
                em.merge(user);
            }
        } catch (Exception e) {
            logger.severe("Error updating last login: " + e.getMessage());
        }
    }
    
    /**
     * Find all users
     */
    public List<Users> findAllUsers() {
        try {
            TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u ORDER BY u.createdAt DESC", Users.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding all users: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Find active users only
     */
    public List<Users> findActiveUsers() {
        try {
            TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u WHERE u.active = true ORDER BY u.fullName", Users.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding active users: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Find users by role
     */
    public List<Users> findUsersByRole(String role) {
        try {
            TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u WHERE u.role = :role ORDER BY u.fullName", Users.class);
            query.setParameter("role", role);
            return query.getResultList();
        } catch (Exception e) {
            logger.severe("Error finding users by role: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Find user by username
     */
    public Users findByUsername(String username) {
        try {
            TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u WHERE u.username = :username", Users.class);
            query.setParameter("username", username);
            List<Users> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logger.severe("Error finding user by username: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Find user by email
     */
    public Users findByEmail(String email) {
        try {
            TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u WHERE u.email = :email", Users.class);
            query.setParameter("email", email);
            List<Users> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logger.severe("Error finding user by email: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Deactivate user
     */
    public void deactivateUser(Integer userId) {
        try {
            Users user = em.find(Users.class, userId);
            if (user != null) {
                user.setActive(false);
                em.merge(user);
            }
        } catch (Exception e) {
            logger.severe("Error deactivating user: " + e.getMessage());
        }
    }
    
    /**
     * Activate user
     */
    public void activateUser(Integer userId) {
        try {
            Users user = em.find(Users.class, userId);
            if (user != null) {
                user.setActive(true);
                em.merge(user);
            }
        } catch (Exception e) {
            logger.severe("Error activating user: " + e.getMessage());
        }
    }
    
    /**
     * Get total users count
     */
    public long getTotalUsers() {
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(u) FROM Users u", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.severe("Error getting total users count: " + e.getMessage());
            return 0;
        }
    }
}