import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Represents a Product entity in the application.
 * Uses JavaFX properties to allow for data binding in TableViews and other UI controls.
 */
public class Product {
    // The name of the product, observable for UI updates
    private final SimpleStringProperty productName;
    
    // The price of the product, observable for UI updates
    private final SimpleIntegerProperty price;

    /**
     * Constructor to initialize a Product object.
     * @param productName The name of the product
     * @param price The price of the product
     */
    public Product(String productName, int price) {
        this.productName = new SimpleStringProperty(productName);
        this.price = new SimpleIntegerProperty(price);
    }

    /**
     * Gets the product name.
     * @return The name as a String
     */
    public String getProductName() { return productName.get(); }
    
    /**
     * Sets the product name.
     * @param value The new name
     */
    public void setProductName(String value) { productName.set(value); }
    
    /**
     * Accessor for the productName property.
     * Used for JavaFX data binding.
     * @return The SimpleStringProperty object
     */
    public SimpleStringProperty productNameProperty() { return productName; }

    /**
     * Gets the product price.
     * @return The price as an int
     */
    public int getPrice() { return price.get(); }
    
    /**
     * Sets the product price.
     * @param value The new price
     */
    public void setPrice(int value) { price.set(value); }
    
    /**
     * Accessor for the price property.
     * Used for JavaFX data binding.
     * @return The SimpleIntegerProperty object
     */
    public SimpleIntegerProperty priceProperty() { return price; }
}