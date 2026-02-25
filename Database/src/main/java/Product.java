import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty; // ADD THIS IMPORT
import javafx.beans.property.SimpleStringProperty;

public class Product {
    private final SimpleIntegerProperty id; 
    private final SimpleStringProperty productName;
    private final SimpleDoubleProperty price;

    public Product(int id, String productName, double price) {
        this.id = new SimpleIntegerProperty(id);
        this.productName = new SimpleStringProperty(productName);
        this.price = new SimpleDoubleProperty(price);
    }

    public int getId() { return id.get(); }
    public String getProductName() { return productName.get(); }
    public double getPrice() { return price.get(); }
    
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty productNameProperty() { return productName; }
    public SimpleDoubleProperty priceProperty() { return price; }
}
