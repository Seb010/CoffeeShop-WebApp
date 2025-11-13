
// Class for holding users items inside of the basket

package purchase;
import java.io.Serializable;

public class BasketItem implements Serializable {
    private int id;
    private String name;
    private String description;
    private double price;
    private int quantity;

    // Constructor
    public BasketItem(int id, String name, String description, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = 1;  // Default quantity
    }

    // Getters and Setters for managing item inside Basket
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Method to increment quantity
    public void incrementQuantity() {
        this.quantity++;
    }
}
