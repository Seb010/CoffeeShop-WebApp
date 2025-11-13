/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package Beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;
//import javax.sql.rowset.CachedRowSet; This will work in JavaEE8 or ealier versions
import javax.sql.rowset.*;

/**
 *
 * @author seb
 */
@Named(value = "contactusBean")
@RequestScoped
public class contactUsBean implements Serializable {

    /**
     * Creates a new instance of ContactBeans
     */
    public contactUsBean() {
        try{
            dataSource=(DataSource) new InitialContext().lookup("jdbc/CoffeeShop");
        } catch (NamingException ex) {
            Logger.getLogger(contactUsBean.class.getName()).log(Level.SEVERE, null, ex);
        } 

    }
    
    private int formID;
    private String FirstName;
    private String LastName;
    private String Email;
    private String Message;
    private Timestamp FormDate;
    
    private String formSentSuccessful;
    private boolean isFormSubmitted = false;

     // allow the server to inject the DataSource
    DataSource dataSource;
    
    // getters / setters
    public int getFormID() {return formID;}
    public void setFormID(int formID) {this.formID = formID;}

    public String getFirstName() {return FirstName;}
    public void setFirstName(String FirstName) {this.FirstName = FirstName;}

    public String getLastName() {return LastName;}
    public void setLastName(String LastName) {this.LastName = LastName;}

    public String getEmail() {return Email;}
    public void setEmail(String Email) {this.Email = Email;}

    public String getMessage() {return Message;}
    public void setMessage(String Message) {this.Message = Message;}

    public Timestamp getFormDate() {return FormDate;}
    public void setFormDate(Timestamp FormDate) {this.FormDate = FormDate;}

    
     public String getFormSentSuccessful() {return formSentSuccessful;}

    public void setFormSentSuccessful(String formSentSuccessful) {this.formSentSuccessful = formSentSuccessful;}
    
    public boolean getIsFormSubmitted() {
        return isFormSubmitted;
    
    }
    public String manageForm() throws SQLException
    {
        
        // Reset for every failed submission
        formSentSuccessful = null;
        isFormSubmitted = false;
        
        //Google re-captcha API Validation retrieve and validate
        String recaptchaResponse = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("g-recaptcha-response");
        
        if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
            System.out.println("recaptchaResponse is null or empty.");
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Captcha verification failed: No response provided.", null));
            return null;
        
        }
                
        // check whether dataSource was injected by the server
       if (dataSource == null)
           throw new SQLException("Unable to obtain data source");
       
       // obtain a connection from the connection pool
       Connection connection = dataSource.getConnection();
       
       // check whether connection was successful
       if (connection == null)
           throw new SQLException("Unable to connect to data source");
       
       try {
           // create a PreparedStatement to insert a new address book entry
           PreparedStatement addFormEntry = 
                   connection.prepareStatement("INSERT INTO CONTACT " +
                           "(FIRST_NAME, LAST_NAME, EMAIL, MESSAGE, FORMDATE)" + 
                           "VALUES( ?, ?, ?, ?, ?)");
           
           // specify the PreparedStatement's arguments
           addFormEntry.setString(1, getFirstName() );
           addFormEntry.setString(2, getLastName() );
           addFormEntry.setString(3, getEmail() );
           addFormEntry.setString(4, getMessage() );
           addFormEntry.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis())); //set date and time form was submitted
           addFormEntry.executeUpdate(); // insert the entry
           
           setFormSentSuccessful("Thank you," + getFirstName() + "! Your message has been sent sucessfully"); // confirmation message
           isFormSubmitted = true;
           
           return null;
   
      
       } catch(SQLException e) {
       
           e.printStackTrace();
           FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", null));
           return null;
       }// end try

       finally
       {
           connection.close();
       } //end finally
       
    } // end method manageForm()
    
    // verify Google re-captcha API
    
    private boolean verifyCaptcha(String recaptchaResponse) {
    String secretKey = "6LdtDQIrAAAAAORFna8sfaQ12JohuhfP0fAlxbeE";
    String apiUrl = "https://www.google.com/recaptcha/api/siteverify";
    try {
        URL url = new URL(apiUrl + "?secret=" + secretKey + "&response=" + recaptchaResponse);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        InputStream responseStream = connection.getInputStream();
        Scanner scanner = new Scanner(responseStream, "UTF-8");
        String responseBody = scanner.useDelimiter("\\A").next();
        scanner.close();

        // Simple substring check
        return responseBody.contains("\"success\": true");
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
    }
    
    public ResultSet getContactForm() throws SQLException
    {
        // check whether dataSource was injected by the server
      if ( dataSource == null )
         throw new SQLException( "Unable to obtain DataSource" );
      
       // obtain a connection from the connection pool
      Connection connection = dataSource.getConnection("APP", "APP");
      
      // check whether connection was successful
      if ( connection == null )
         throw new SQLException( "Unable to connect to DataSource" );
      
      try {
          PreparedStatement getContactForm = connection.prepareStatement(
          "SELECT ID, FIRST_NAME, LAST_NAME, EMAIL, MESSAGE, FORMDATE " +
                  "FROM CONTACT ORDER BY ID");
          
          
          
           //CachedRowSet rowSet = new com.sun.rowset.CachedRowSetImpl(); this will work in Java EE8 or earlier versions
         CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
         rowSet.populate( getContactForm.executeQuery() );
         
         return rowSet; 
      } // end try
      finally {
          connection.close(); // return connection to pool
      } // end finally
    } // end method 
    
    public int getTotalContactForms() {
    int count = 0;

    if (dataSource == null) {
        Logger.getLogger(contactUsBean.class.getName()).log(Level.SEVERE, "DataSource not found!");
        return count;
    }
    
    // SQL to display total contact form submissions in Admin Dashboard Page
    try (Connection conn = dataSource.getConnection("APP", "APP")) {
        String sql = "SELECT COUNT(*) FROM CONTACT";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            count = rs.getInt(1);
        }
    } catch (SQLException e) {
        Logger.getLogger(contactUsBean.class.getName()).log(Level.SEVERE, "Error retrieving contact form count", e);
    }

    return count;
}
} // end class


    
  

