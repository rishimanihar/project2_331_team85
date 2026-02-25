package app.src.main.java;
import javafx.beans.property.*;

public class Models {
    public static class MenuItem {
        public SimpleIntegerProperty id;
        public SimpleStringProperty name;
        public SimpleDoubleProperty price;
        public MenuItem(int id, String name, double price) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
        }
    }

    public static class InventoryItem {
        public SimpleIntegerProperty id;
        public SimpleStringProperty name;
        public SimpleIntegerProperty quantity;
        public SimpleStringProperty unit;
        public InventoryItem(int id, String name, int quantity, String unit) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.unit = new SimpleStringProperty(unit);
        }
    }

    public static class Employee {
        public SimpleIntegerProperty id;
        public SimpleStringProperty name;
        public SimpleStringProperty role;
        public Employee(int id, String name, String role) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.role = new SimpleStringProperty(role);
        }
    }
}