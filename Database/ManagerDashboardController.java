import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ManagerDashboardController {

    // Schedule UI
    @FXML private ListView<String> scheduleList;

    // Inventory UI
    @FXML private TableView<Models.InventoryItem> inventoryTable;
    @FXML private TableColumn<Models.InventoryItem, String> invNameCol;
    @FXML private TableColumn<Models.InventoryItem, Integer> invQtyCol;
    @FXML private TableColumn<Models.InventoryItem, String> invUnitCol;

    // Trends UI
    @FXML private LineChart<String, Number> trendsChart;

    // Overview UI
    @FXML private Label revenueLabel;
    @FXML private Label ordersLabel;

    // Chat UI
    @FXML private ListView<String> chatList;
    @FXML private TextField chatInput;
    @FXML private Button sendButton;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";

    @FXML
    public void initialize() {
        // Setting Inventory 
        invNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        invQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        invUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        // Load Data
        loadInventory();
        loadItemTrends();
        loadQuarterlyOverview();
        loadSchedule();
        
        chatList.getItems().add("System: Welcome to the Manager Dashboard.");
        
        // Chat functionality
        sendButton.setOnAction(e -> sendMessage());
        chatInput.setOnAction(e -> sendMessage()); 
    }

    private void sendMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            chatList.getItems().add("Manager: " + message);
            chatInput.clear(); 
        }
    }

    private void loadInventory() {
        ObservableList<Models.InventoryItem> invList = FXCollections.observableArrayList();
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            
            // Querying the inventory table
            ResultSet rs = stmt.executeQuery("SELECT id, item_name, quantity, unit FROM inventory");
            while (rs.next()) {
                invList.add(new Models.InventoryItem(
                    rs.getInt("id"),
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
        trendsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales (Last 14 Days)");
        
        // Queries the orders table
        String sql = "SELECT TRIM(to_char(order_timestamp, 'Day')) AS day_of_week, SUM(total_amount) AS total_revenue " +
                    "FROM orders " +
                    "GROUP BY day_of_week, EXTRACT(ISODOW FROM order_timestamp) " +
                    "ORDER BY EXTRACT(ISODOW FROM order_timestamp)";
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
             
            List<XYChart.Data<String, Number>> dataPoints = new ArrayList<>();
            while (rs.next()) {
                String day = rs.getString("day_of_week");
                double revenue = rs.getDouble("total_revenue");
                System.out.println(day + ": $" + String.format("%.2f", revenue));
                dataPoints.add(new XYChart.Data<>(day, revenue));
            }
            Collections.reverse(dataPoints);
            series.getData().addAll(dataPoints);
            trendsChart.getData().add(series);
            
            conn.close();
        } catch (Exception e) { 
            e.printStackTrace(); 
            System.out.println("Chart Error: Double check your 'orders' column names!");
        }
    }

    private void loadQuarterlyOverview() {
        ordersLabel.setText("1,245");
        revenueLabel.setText("24,500.00");
    }

    private void loadSchedule() {
        scheduleList.getItems().clear();
        List<String> employeeNames = new ArrayList<>();
        
        // Fetch employees
        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, role FROM employees");
            
            while (rs.next()) {
                employeeNames.add(rs.getString("role"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Randomly shift assignment
        if (employeeNames.isEmpty()) {
            scheduleList.getItems().add("No employees found in database.");
            return;
        }

        String[] days = {"Mon", "Tue", "Wed", "Thr", "Fri", "Sat", "Sun"};
        Random rand = new Random();
        
        for (String day : days) {
            String morningEmp = employeeNames.get(rand.nextInt(employeeNames.size()));
            String eveningEmp = employeeNames.get(rand.nextInt(employeeNames.size()));    
            scheduleList.getItems().add(day + ": " + morningEmp + " (Morning) | " + eveningEmp + " (Evening)");
        }
    }
}