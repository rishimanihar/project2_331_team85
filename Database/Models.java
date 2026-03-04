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

    public static class SalesItem {
        private String itemName;
        private int quantity;
        private double revenue;

        public SalesItem(String itemName, int quantity, double revenue) {
            this.itemName = itemName;
            this.quantity = quantity;
            this.revenue = revenue;
        }

        public String getItemName() { return itemName; }
        public int getQuantity() { return quantity; }
        public double getRevenue() { return revenue; }
    }
}