/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package Beans;
import purchase.MenuItem;
import purchase.CartItem;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@Named(value = "menuBeans")
@SessionScoped
public class menuBean implements Serializable {

    //private static final long serialVersionUID = 1L;
    
    private int orderId;

    private List<MenuItem> menu = new ArrayList<>();
    private List<CartItem> basket = new ArrayList<>();
    
    private DataSource dataSource;
    private String collectionTime;  // The time selected by the user (no date)
    private List<String> availableTimes;  // Available times to display in the dropdown

    @Inject
    private loginBean loginBean;  // Inject the loginBean to access the logged-in user's ID

    // Constructor
    public menuBean() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("jdbc/CoffeeShop");
            loadMenu();  // Load menu when the bean is created
            loadAvailableTimes();  // Load available times for collection
        } catch (NamingException ex) {
            Logger.getLogger(menuBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public int getOrderId() {
        return orderId;
    
    }
    

    // Getter for the menu
    public List<MenuItem> getMenu() {
        return menu;
    }

    // Getter for the basket
    public List<CartItem> getBasket() {
        return basket;
    }

    // Getter for today's date in string format (YYYY-MM-DD)
    public String getToday() {
        return LocalDate.now().toString();  // Returns today's date in the format YYYY-MM-DD
    }

    // Getter and setter for collectionTime (time part only)
    public String getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(String collectionTime) {
        this.collectionTime = collectionTime;
    }

    // Getter for available times
    public List<String> getAvailableTimes() {
        return availableTimes;
    }

    // Method to load available times for collection 
    private void loadAvailableTimes() {
        availableTimes = new ArrayList<>();
        
        availableTimes.add("08:00");
        availableTimes.add("08:30");
        availableTimes.add("09:00");
        availableTimes.add("09:30");
        availableTimes.add("10:00");
        availableTimes.add("10:30");
        availableTimes.add("11:00");
        availableTimes.add("11:30");
        availableTimes.add("12:00");
        availableTimes.add("12:30");
        availableTimes.add("13:00");
        availableTimes.add("13:30");
        availableTimes.add("14:00");
        availableTimes.add("14:30");
        availableTimes.add("15:00");
        availableTimes.add("15:30");
        availableTimes.add("16:00");
        availableTimes.add("16:30");
        availableTimes.add("17:00");
    }

    // Load menu items from the database
    private void loadMenu() {
        if (dataSource == null) {
            throw new RuntimeException("Unable to obtain DataSource");
        }

        String sql = "SELECT id, name, description, price, available FROM products ORDER BY name"; // a-z
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            
            while (rs.next()) {
                // Create a new MenuItem and add it to the menu list
                MenuItem item = new MenuItem();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setPrice(rs.getDouble("price"));
                item.setAvailable(rs.getBoolean("available"));
                menu.add(item);
            }
        } catch (SQLException ex) {
            Logger.getLogger(menuBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Add an item to the basket
    public void addToBasket(int productID, String name, String description, double price) {
        CartItem item = findItemInBasket(productID);
        if (item == null) {
            // Item is not in the basket, add new item
            basket.add(new CartItem(productID, name, description, price, 1));
        } else {
            // Item is already in the basket, increase quantity
            item.setQuantity(item.getQuantity() + 1);
        }
    }

    // Helper method to find an item in the basket by its product ID
    private CartItem findItemInBasket(int productID) {
        for (CartItem item : basket) {
            if (item.getProductID() == productID) {
                return item;
            }
        }
        return null;
    }

    // Calculate the total cost of the basket
    public double getTotalCost() {
        double total = 0;
        for (CartItem item : basket) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }
    
    // Method to remove one item from the basket
    public void removeFromBasket(int productID) {
        CartItem item = findItemInBasket(productID);
        if (item != null) {
            if (item.getQuantity() > 1) {
                // Decrease the quantity by 1 if the quantity is greater than 1
                item.setQuantity(item.getQuantity() - 1);
            } else {
                // Remove the item from the basket if the quantity is 1
                basket.remove(item);
            }
        }
    }
    
    public void clearBasket() {
        basket.clear();
    
    }

    // Confirm the order
    public String confirmOrder() {
        // Get today's date 
        String todayDate = LocalDate.now().toString(); // E.g., "2025-04-06"
        
        // Combine today's date with the selected collection time
        String fullCollectionTime = todayDate + " " + collectionTime; 
        
        // Ensure the time is in the correct format
        if (collectionTime.length() == 5) {  // If the time format is "HH:mm"
            fullCollectionTime += ":00";  // Append seconds if missing
        }
        // Convert to Timestamp
        try {
            Timestamp timestamp = Timestamp.valueOf(fullCollectionTime);  // Convert string to Timestamp
            
            // Save the order to the database
            String sqlOrder = "INSERT INTO orders (user_id, total_price, collection_time, status) VALUES (?, ?, ?, ?)";
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement psOrder = connection.prepareStatement(sqlOrder, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, loginBean.getLoggedInUserId());  // Get current user's ID from loginBean
                psOrder.setDouble(2, getTotalCost());
                psOrder.setTimestamp(3, timestamp);  // Store the complete datetime
                psOrder.setString(4, "PENDING");
                psOrder.executeUpdate();

                ResultSet rs = psOrder.getGeneratedKeys();
                if (rs.next()) {
                     // Get generated order ID
                    this.orderId = rs.getInt(1);
                    // Save order items
                    String sqlOrderItem = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psOrderItem = connection.prepareStatement(sqlOrderItem)) {
                        for (CartItem item : basket) {
                            psOrderItem.setInt(1, orderId);
                            psOrderItem.setInt(2, item.getProductID());
                            psOrderItem.setInt(3, item.getQuantity());
                            psOrderItem.setDouble(4, item.getPrice());
                            psOrderItem.addBatch();
                        }
                        psOrderItem.executeBatch();  // Execute insert for order items
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(menuBean.class.getName()).log(Level.SEVERE, null, ex);
            }

            return "user_orderConfirmation.xhtml?faces-redirect=true";  // Redirect to confirmation page
        } catch (IllegalArgumentException e) {
            // Handle invalid timestamp format
            e.printStackTrace();
            return null;  // Stay on the same page
        }
    }

    // Method to clear the basket after confirmation page
    public String clearBasketAfterCheckout() {
        basket.clear();
        return "user_profile.xhtml?faces-redirect=true";
        
    }
}