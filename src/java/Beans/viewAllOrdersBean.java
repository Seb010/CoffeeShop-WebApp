/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package Beans;
import purchase.Order;
import purchase.OrderItem;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@Named(value = "viewOrdersBean")
@ViewScoped
public class viewAllOrdersBean implements Serializable {

    private DataSource dataSource;
    private static final Logger LOGGER = Logger.getLogger(viewAllOrdersBean.class.getName());

    public viewAllOrdersBean() {
        try {
            // Look up the data source using JNDI
            dataSource = (DataSource) new InitialContext().lookup("jdbc/CoffeeShop");
        } catch (NamingException ex) {
            LOGGER.log(Level.SEVERE, "DataSource not found", ex);
        }
    }

    // Method to fetch all orders
    public List<Order> getAllOrdersForm() {
        List<Order> orders = new ArrayList<>();

        if (dataSource == null) {
            LOGGER.log(Level.SEVERE, "DataSource not found!");
            return orders;
        }

        try (Connection conn = dataSource.getConnection("APP", "APP")) {
            String sql = "SELECT ID, USER_ID, TOTAL_PRICE, STATUS, COLLECTION_TIME, ORDER_DATE " +
                         "FROM ORDERS ORDER BY ID ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("ID"));
                order.setUserId(rs.getInt("USER_ID"));
                order.setTotalPrice(rs.getDouble("TOTAL_PRICE"));
                order.setStatus(rs.getString("STATUS"));
                order.setCollectionTime(rs.getTimestamp("COLLECTION_TIME"));
                order.setOrderDate(rs.getTimestamp("ORDER_DATE"));
                orders.add(order);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving orders", e);
        }

        return orders;
    }

    // Method to fetch items for a given order
    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        List<OrderItem> items = new ArrayList<>();

        if (dataSource == null) return items;
        
        // retrives product information from order_items table
        try (Connection conn = dataSource.getConnection("APP", "APP")) {
            String sql = "SELECT P.name AS PRODUCT_NAME, OI.QUANTITY, OI.PRICE " +
                         "FROM ORDER_ITEMS OI " +
                         "JOIN PRODUCTS P ON OI.PRODUCT_ID = P.ID " +
                         "WHERE OI.ORDER_ID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setProductName(rs.getString("PRODUCT_NAME"));
                item.setQuantity(rs.getInt("QUANTITY"));
                item.setPrice(rs.getDouble("PRICE"));
                items.add(item);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving order items", e);
        }

        LOGGER.log(Level.INFO, "Fetched {0} items for order ID {1}", new Object[]{items.size(), orderId});
        return items;
    }

    // Method to mark an order as "COMPLETE"
    public void markOrderComplete(int orderId) {
        if (dataSource == null) {
            LOGGER.log(Level.SEVERE, "DataSource not found!");
            return;
        }

        try (Connection conn = dataSource.getConnection("APP", "APP")) {
            // SQL query to update the order's status to "COMPLETE"
            String sql = "UPDATE ORDERS SET STATUS = ? WHERE ID = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "COMPLETE");
            ps.setInt(2, orderId);

            // Execute the update
            int rowsUpdated = ps.executeUpdate();
            
            if (rowsUpdated > 0) {
                LOGGER.log(Level.INFO, "Order {0} marked as COMPLETE", orderId);
            } else {
                LOGGER.log(Level.WARNING, "Order {0} not found", orderId);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating order status", e);
        }
    }
    // method to display total completed orders in admin dashboard
    public int getCompletedOrdersCount() {
    int count = 0;
    if (dataSource == null) {
        LOGGER.log(Level.SEVERE, "DataSource not found!");
        return count;
    }

    try (Connection conn = dataSource.getConnection("APP", "APP")) {
        String sql = "SELECT COUNT(*) FROM ORDERS WHERE STATUS = 'COMPLETE'";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            count = rs.getInt(1);
        }
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error retrieving completed orders count", e);
    }

    return count;
}
    // method to display outstanding orders in the admin dashboard page
    public int getPendingOrdersCount() {
    int count = 0;
    if (dataSource == null) {
        LOGGER.log(Level.SEVERE, "DataSource not found!");
        return count;
    }

    try (Connection conn = dataSource.getConnection("APP", "APP")) {
        String sql = "SELECT COUNT(*) FROM ORDERS WHERE STATUS = 'PENDING'";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            count = rs.getInt(1);
        }
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error retrieving pending orders count", e);
    }

    return count;
}
    // Admin Page to display only completed orders
    public List<Order> getCompletedOrders() {
    List<Order> orders = new ArrayList<>();

    if (dataSource == null) {
        LOGGER.log(Level.SEVERE, "DataSource not found!");
        return orders;
    }

    try (Connection conn = dataSource.getConnection("APP", "APP")) {
        String sql = "SELECT ID, USER_ID, TOTAL_PRICE, STATUS, COLLECTION_TIME, ORDER_DATE " +
                     "FROM ORDERS WHERE STATUS = 'COMPLETE' ORDER BY ID ASC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Order order = new Order();
            order.setId(rs.getInt("ID"));
            order.setUserId(rs.getInt("USER_ID"));
            order.setTotalPrice(rs.getDouble("TOTAL_PRICE"));
            order.setStatus(rs.getString("STATUS"));
            order.setCollectionTime(rs.getTimestamp("COLLECTION_TIME"));
            order.setOrderDate(rs.getTimestamp("ORDER_DATE"));
            orders.add(order);
        }

    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error retrieving completed orders", e);
    }

    return orders;
}
    
    // Admin Page to display only pending orders
    public List<Order> getPendingOrders() {
    List<Order> orders = new ArrayList<>();

    if (dataSource == null) {
        LOGGER.log(Level.SEVERE, "DataSource not found!");
        return orders;
    }

    try (Connection conn = dataSource.getConnection("APP", "APP")) {
        String sql = "SELECT ID, USER_ID, TOTAL_PRICE, STATUS, COLLECTION_TIME, ORDER_DATE " +
                     "FROM ORDERS WHERE STATUS = 'PENDING' ORDER BY ID ASC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Order order = new Order();
            order.setId(rs.getInt("ID"));
            order.setUserId(rs.getInt("USER_ID"));
            order.setTotalPrice(rs.getDouble("TOTAL_PRICE"));
            order.setStatus(rs.getString("STATUS"));
            order.setCollectionTime(rs.getTimestamp("COLLECTION_TIME"));
            order.setOrderDate(rs.getTimestamp("ORDER_DATE"));
            orders.add(order);
        }

    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error retrieving pending orders", e);
    }

    return orders;
}



}
