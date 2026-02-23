public class MenuItem {
    private String name;
    private double price;

    public MenuItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // Getters are required for the TableView to display data
    public String getName() { return name; }
    public double getPrice() { return price; }
}
