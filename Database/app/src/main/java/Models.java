public class Models {

    public static class MenuItem {
        private int id;
        private String name;
        private double price;

        public MenuItem(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
    }

    public static class InventoryItem {
        private int id;
        private String name;
        private int quantity;
        private String unit;

        public InventoryItem(int id, String name, int quantity, String unit) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public String getUnit() { return unit; }
    }
}