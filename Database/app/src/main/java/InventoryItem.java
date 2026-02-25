package app.src.main.java;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class InventoryItem {
    private final SimpleStringProperty itemName;
    private final SimpleIntegerProperty quantity;
    private final SimpleStringProperty unit;

    public InventoryItem(String itemName, int quantity, String unit) {
        this.itemName = new SimpleStringProperty(itemName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unit = new SimpleStringProperty(unit);
    }

    public String getItemName() { return itemName.get(); }
    public int getQuantity() { return quantity.get(); }
    public String getUnit() { return unit.get(); }
}