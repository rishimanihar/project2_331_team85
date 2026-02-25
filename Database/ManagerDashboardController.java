import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ManagerDashboardController {

    // All the FXML Components (Ag)
    @FXML private ListView<String> scheduleList;
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> invNameCol;
    @FXML private TableColumn<InventoryItem, Integer> invQtyCol;
    @FXML private TableColumn<InventoryItem, String> invUnitCol;
    @FXML private LineChart<String, Number> trendsChart;
    @FXML private Label revenueLabel;
    @FXML private Label ordersLabel;
    @FXML private ListView<String> chatList;
    @FXML private TextField chatInput;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";

    @FXML
    public void initialize() {
        invNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        invQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        invUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        loadInventory();
        loadItemTrends();
        loadQuarterlyOverview();
        
        // Sample employees with our names (Ag)
        scheduleList.getItems().addAll("Mon: Rishi (Morning), Mo (Evening)", "Tue: Brayden (Morning), Arul (Evening)", "Wed: Aayush (Morning), Mo (Evening)", "Thu: Brayden (Morning), Arul (Evening)", "Fri: Rishi (Morning), Aayush (Evening)", "Sat: Mo (Morning), Brayden (Evening)", "Sun: Arul (Morning), Aayush (Evening)");
        chatList.getItems().add("System: Welcome to the Manager Dashboard.");
    }

    private void loadInventory() {
        ObservableList<InventoryItem> invList = FXCollections.observableArrayList();
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            
            // Getting all the inventory items from the database (Ag)
            ResultSet rs = stmt.executeQuery("SELECT item_name, quantity, unit FROM inventory");
            while (rs.next()) {
                invList.add(new InventoryItem(
                    rs.getString("item_name"),
                    rs.getInt("quantity"),
                    rs.getString("unit")
                ));
            }
            inventoryTable.setItems(invList);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadItemTrends() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales");

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            
            // Getting the daily revenue for the last 14 days to show trends (Ag)
            String sql = "SELECT DATE(order_timestamp) as order_date, SUM(total_amount) as daily_revenue " +
                         "FROM orders " +
                         "GROUP BY DATE(order_timestamp) " +
                         "ORDER BY order_date ASC LIMIT 14"; 
                         
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                    rs.getString("order_date"), 
                    rs.getDouble("daily_revenue")
                ));
            }
            trendsChart.getData().add(series);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadQuarterlyOverview() {
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            
            // Getting the orders table to calc total orders and revenue (Ag)
            ResultSet rs = stmt.executeQuery("SELECT COUNT(id) as total_orders, SUM(total_amount) as total_rev FROM orders");
            if (rs.next()) {
                ordersLabel.setText(String.valueOf(rs.getInt("total_orders")));
                revenueLabel.setText(String.format("$%.2f", rs.getDouble("total_rev")));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

