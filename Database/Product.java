import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Product {
    private final SimpleStringProperty productName;
    private final SimpleIntegerProperty price;

    public Product(String productName, int price) {
        this.productName = new SimpleStringProperty(productName);
        this.price = new SimpleIntegerProperty(price);
    }

    public String getProductName() { return productName.get(); }
    public void setProductName(String value) { productName.set(value); }
    public SimpleStringProperty productNameProperty() { return productName; }

    public int getPrice() { return price.get(); }
    public void setPrice(int value) { price.set(value); }
    public SimpleIntegerProperty priceProperty() { return price; }
}