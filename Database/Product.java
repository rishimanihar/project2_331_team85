import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Product {
    private final SimpleStringProperty productName;
    private final SimpleDoubleProperty price;

    public Product(String productName, double price) {
        this.productName = new SimpleStringProperty(productName);
        this.price = new SimpleDoubleProperty(price);
    }

    // Standard Getters (Required for TableView)
    public String getProductName() { return productName.get(); }
    public double getPrice() { return price.get(); }

    // Property Getters (Required for advanced JavaFX features)
    public SimpleStringProperty productNameProperty() { return productName; }
    public SimpleDoubleProperty priceProperty() { return price; }
}