/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package Beans;


import static com.sun.xml.ws.spi.db.BindingContextFactory.LOGGER;
import java.util.logging.Level;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.mindrot.jbcrypt.BCrypt;

@Named(value = "loginBean")
@SessionScoped
public class loginBean implements Serializable {


    //private static final long serialVersionUID = 1L;


    private menuBean menuBean; 

    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;

    private boolean loggedIn;
    private boolean incorrectUsername;
    private boolean incorrectPassword;

    private int loggedInUserId;  // Store the logged-in user's ID

    private DataSource dataSource;

    // Constructor
    public loginBean() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("jdbc/CoffeeShop");
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
    }
    
    // Getters / Setters
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

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isIncorrectUsername() {
        return incorrectUsername;
    }

    public boolean isIncorrectPassword() {
        return incorrectPassword;
    }

    public int getLoggedInUserId() {
        return loggedInUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRole() {
        return role;
    }

    // Login method
    public String login() {
        // Reset error flags at the start
        incorrectUsername = false;
        incorrectPassword = false;

        if (dataSource == null) {
            return null;  // Stay on login page if data source is null
        }

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT ID, PASSWORD, FULL_NAME, EMAIL, PHONE_NUMBER, ROLE FROM users WHERE USERNAME = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Username exists, now check the password
                    String storedPassword = rs.getString("PASSWORD");
                    if (BCrypt.checkpw(password, storedPassword)) { // check if password matches encrypted password
                        // Login successful, set profile details
                        fullName = rs.getString("FULL_NAME");
                        email = rs.getString("EMAIL");
                        phoneNumber = rs.getString("PHONE_NUMBER");
                        role = rs.getString("ROLE");
                        loggedInUserId = rs.getInt("ID");  // Store the user ID
                        loggedIn = true;

                        // Check the role and redirect accordingly
                        if ("Admin".equalsIgnoreCase(role.trim())) {
                            return "admin_dashboard.xhtml?faces-redirect=true"; // Redirect to the admin dashboard
                        } else {
                            return "user_profile.xhtml?faces-redirect=true"; // Redirect to the user profile page
                        }
                    } else {
                        // Incorrect password
                        incorrectPassword = true;
                        return null; // Stay on the login page
                    }
                } else {
                    // Username does not exist
                    incorrectUsername = true;
                    return null; // Stay on the login page
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Stay on the login page
        }
    }

    // Logout method
    public String logout() {
        loggedIn = false;
        loggedInUserId = 0;  // Reset the logged-in user ID
        menuBean.clearBasket(); //clear Basket when different user logs in
        return "login.xhtml?faces-redirect=true";  // Redirect to login page
    }
    
    
    // method to display total users in admin dashboard page
    public int getTotalRegisteredUsers() {
    int count = 0;
    if (dataSource == null) {
        LOGGER.log(Level.SEVERE, "DataSource not found!");
        return count;
    }

    try (Connection conn = dataSource.getConnection("APP", "APP")) {
        String sql = "SELECT COUNT(*) FROM USERS";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            count = rs.getInt(1);
        }
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error retrieving user count", e);
    }

    return count;
}
    
    // method to display total admin in admin dashboard page

public int getTotalAdmins() {
    int count = 0;
    if (dataSource == null) {
        LOGGER.log(Level.SEVERE, "DataSource not found!");
        return count;
    }

    try (Connection conn = dataSource.getConnection("APP", "APP")) {
        String sql = "SELECT COUNT(*) FROM USERS WHERE ROLE = 'Admin'";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            count = rs.getInt(1);
        }
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error retrieving admin count", e);
    }

    return count;
}

}