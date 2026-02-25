import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;

public class ManagerDashboardController {

    // --- FXML UI Elements (Manager Side) ---
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> invNameCol;
    @FXML private TableColumn<InventoryItem, Integer> invQtyCol;
    @FXML private TableColumn<InventoryItem, String> invUnitCol;
    @FXML private LineChart<String, Number> trendsChart;
    @FXML private Label revenueLabel;
    @FXML private Label ordersLabel;
    @FXML private ListView<String> scheduleList;

    // --- FXML UI Elements (Cashier Side) ---
    @FXML private FlowPane buttonContainer;
    @FXML private TableView<Product> cartList;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private Label totalLabel;
    @FXML private Button queryButton;
    @FXML private Button submitOrderButton;

    // --- Data and Connection ---
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";
    private ObservableList<Product> cartData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        invNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        invQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        invUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        cartList.setItems(cartData);

        loadInventory();
        loadItemTrends();
        loadQuarterlyOverview();
        loadMenuButtons();

        if (queryButton != null) {
            queryButton.setOnAction(event -> loadMenuButtons());
        }

        scheduleList.getItems().addAll(
            "Mon: Rishi (Morning), Mo (Evening)",
            "Tue: Brayden (Morning), Arul (Evening)",
            "Wed: Aayush (Morning), Mo (Evening)",
            "Thu: Brayden (Morning), Arul (Evening)",
            "Fri: Rishi (Morning), Aayush (Evening)",
            "Sat: Mo (Morning), Brayden (Evening)",
            "Sun: Arul (Morning), Aayush (Evening)"
        );
    }

    private void loadMenuButtons() {
        buttonContainer.getChildren().clear();
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT item_name, price FROM menu")) {

            while (rs.next()) {
                String name = rs.getString("item_name");
                double price = rs.getDouble("price");

                Button itemButton = new Button(name + "\n$" + String.format("%.2f", price));
                itemButton.setPrefSize(120, 80);
                itemButton.setOnAction(e -> {
                    cartData.add(new Product(name, price));
                    updateTotal();
                    System.out.println("[LOG] Added " + name + " to cart.");
                });
                buttonContainer.getChildren().add(itemButton);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to load menu: " + e.getMessage());
        }
    }

    private void updateTotal() {
        double total = 0;
        for (Product item : cartData) {
            total += item.getPrice();
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void loadInventory() {
        ObservableList<InventoryItem> invList = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT item_name, quantity, unit FROM inventory")) {

            while (rs.next()) {
                invList.add(new InventoryItem(
                    rs.getString("item_name"),
                    rs.getInt("quantity"),
                    rs.getString("unit")
                ));
            }
            inventoryTable.setItems(invList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadItemTrends() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales");

        // Uses order_time (cast to date) and total_price from your actual schema
        String sql = "SELECT order_time::date as order_date, SUM(total_price) as daily_revenue " +
                     "FROM orders GROUP BY order_date " +
                     "ORDER BY order_date ASC LIMIT 14";

        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("order_date"), rs.getDouble("daily_revenue")));
            }
            trendsChart.getData().add(series);
        } catch (SQLException e) {
            System.err.println("[SQL ERROR] Trends: " + e.getMessage());
        }
    }

    private void loadQuarterlyOverview() {
        // Uses order_id and total_price from your actual schema
        String sql = "SELECT COUNT(order_id) as total_orders, SUM(total_price) as total_rev FROM orders";

        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                ordersLabel.setText(String.valueOf(rs.getInt("total_orders")));
                revenueLabel.setText(String.format("$%.2f", rs.getDouble("total_rev")));
            }
        } catch (SQLException e) {
            System.err.println("[SQL ERROR] Overview: " + e.getMessage());
        }
    }
}
