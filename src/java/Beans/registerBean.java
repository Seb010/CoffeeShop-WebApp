/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package Beans;

import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author seb
 */
@Named(value = "registerBean")
@ViewScoped
public class registerBean implements Serializable {

    /**
     * Creates a new instance of registerBeans
     */
    public registerBean() {
        try {
            dataSource=(DataSource) new InitialContext().lookup("jdbc/CoffeeShop");
        } catch (NamingException ex) {
            Logger.getLogger(registerBean.class.getName()).log(Level.SEVERE, null, ex);
        
        }
    }
    
  
    private String username;
    private String password;
    private String role = "Customer"; //default role
    private String fullname;
    private String email;
    private String phone;
    
    private boolean signupFailed = false;
    private boolean signupSuccess = false;
    
    // allow the server to inject the DataSource
    DataSource dataSource;


    //Getters / Setters
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

    public String getFullname() {return fullname;}
    public void setFullname(String fullname) {this.fullname = fullname;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getPhone() {return phone;}
    public void setPhone(String phone) {this.phone = phone;}
    
    public boolean isSignupFailed() { return signupFailed; }  
    public boolean isSignupSuccess() { return signupSuccess; }
    
    
    public String register() {
        if (dataSource == null) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Database Error", null));
            return null;
        }

        try (Connection connection = dataSource.getConnection()) {
            if (connection == null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Database Connection Failed", null));
                return null;
            }

            // Hash Password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            String sql = "INSERT INTO users (USERNAME, PASSWORD, ROLE, FULL_NAME, EMAIL, PHONE_NUMBER) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, hashedPassword);
                stmt.setString(3, role);
                stmt.setString(4, fullname);
                stmt.setString(5, email);
                stmt.setString(6, phone);

                stmt.executeUpdate();
                connection.commit();
                
                signupSuccess = true;
                signupFailed = false;

                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Registration Successful!", null));

                return "view_login.xhtml"; //if successful registeration
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.getLogger(registerBean.class.getName()).log(Level.SEVERE, "SQL Error: " + e.getMessage(), e);
            
            signupFailed = true;
            signupSuccess = false;
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sign Up Failed! Username or Email might be taken.", null));
            return null;
        }
    }
}
