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

    @FXML private TextField invNameInput;
    @FXML private TextField invQtyInput;
    @FXML private TextField invUnitInput;
    @FXML private Button addInvBtn;
    @FXML private Label invStatusLabel; 

    // Trends UI
    @FXML private LineChart<String, Number> trendsChart;

    // Overview UI
    @FXML private Label revenueLabel;
    @FXML private Label ordersLabel;

    // Chat UI
    @FXML private ListView<String> chatList;
    @FXML private TextField chatInput;
    @FXML private Button sendButton;

    // Menu update UI (not implemented yet)
    // Menu Management UI
    @FXML private TableView<Models.MenuItem> menuTable;
    @FXML private TableColumn<Models.MenuItem, Integer> menuIdCol;
    @FXML private TableColumn<Models.MenuItem, String> menuNameCol;
    @FXML private TableColumn<Models.MenuItem, Double> menuPriceCol;
    @FXML private TextField menuNameInput;
    @FXML private TextField menuPriceInput;
    @FXML private Button addMenuBtn;
    @FXML private Label menuStatusLabel;

    // Employee UI
    @FXML private TextField empNameInput;
    @FXML private TextField empRoleInput;
    @FXML private Button addEmpBtn;
    @FXML private Label empStatusLabel;


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

        // Menu management
        menuIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        menuNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        menuPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Load the menu from DB and set button action
        loadMenuData();
        addMenuBtn.setOnAction(e -> addMenuItemToDB());

        // Inventory addition
        addInvBtn.setOnAction(e -> addInventoryItemToDB());

        // Employee addition
        addEmpBtn.setOnAction(e -> addEmployeeToDB());
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
            Connection conn = getConnection();
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
        String sql = "SELECT TRIM(to_char(order_time, 'Day')) AS day_of_week, SUM(total_price) AS total_revenue " +
                    "FROM orders " +
                    "GROUP BY day_of_week, EXTRACT(ISODOW FROM order_time) " +
                    "ORDER BY EXTRACT(ISODOW FROM order_time)";
        try {
            dbSetup my = new dbSetup();
            Connection conn = getConnection();
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
        String sql = "SELECT COUNT(order_id) as total_orders, SUM(total_price) as total_rev " +
                    "FROM orders " +
                    "WHERE order_time >= NOW() - INTERVAL '3 months'";
                     
        try {
            dbSetup my = new dbSetup();
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                ordersLabel.setText(String.valueOf(rs.getInt("total_orders")));
                revenueLabel.setText(String.format("%.2f", rs.getDouble("total_rev")));
            }
            conn.close();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void loadSchedule() {
        scheduleList.getItems().clear();
        List<String> employeeNames = new ArrayList<>();
        
        // Fetch employees
        try {
            dbSetup my = new dbSetup();
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, role FROM employees");
            
            while (rs.next()) {
                employeeNames.add(rs.getString("name"));
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

    private void loadMenuData() {
        ObservableList<Models.MenuItem> menuList = FXCollections.observableArrayList();
        try {
            dbSetup my = new dbSetup();
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            
            // Query your menu table
            ResultSet rs = stmt.executeQuery("SELECT id, item_name, price FROM menu ORDER BY id ASC");
            while (rs.next()) {
                menuList.add(new Models.MenuItem(
                    rs.getInt("id"),
                    rs.getString("item_name"),
                    rs.getDouble("price")
                ));
            }
            menuTable.setItems(menuList);
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMenuItemToDB() {
        String name = menuNameInput.getText().trim();
        String priceText = menuPriceInput.getText().trim();

        if (name.isEmpty() || priceText.isEmpty()) {
            menuStatusLabel.setText("Error: Please fill in both fields.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            
            dbSetup my = new dbSetup();
            Connection conn = getConnection();
            
            String sql = "INSERT INTO menu (id, item_name, price) VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM menu), ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            
            pstmt.executeUpdate(); 
            conn.close();
            
            menuStatusLabel.setText("Success: Added " + name + " to the database!");
            
            menuNameInput.clear();
            menuPriceInput.clear();
            
            loadMenuData();

        } catch (NumberFormatException nfe) {
            menuStatusLabel.setText("Error: Price must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            menuStatusLabel.setText("Database Error: Check console.");
        }
    }

    private void addInventoryItemToDB() {
        String name = invNameInput.getText().trim();
        String qtyText = invQtyInput.getText().trim();
        String unit = invUnitInput.getText().trim();

        if (name.isEmpty() || qtyText.isEmpty() || unit.isEmpty()) {
            invStatusLabel.setText("Error: Please fill in all fields.");
            invStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            Connection conn = getConnection();
            
            String sql = "INSERT INTO inventory (id, item_name, quantity, unit) VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM inventory), ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, qty);
            pstmt.setString(3, unit);
            
            pstmt.executeUpdate(); 
            conn.close();
            
            invStatusLabel.setText("Success: Added " + name + "!");
            invStatusLabel.setStyle("-fx-text-fill: green;");
            
            invNameInput.clear();
            invQtyInput.clear();
            invUnitInput.clear();
            
            loadInventory();

        } catch (NumberFormatException nfe) {
            invStatusLabel.setText("Error: Quantity must be a whole number.");
            invStatusLabel.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            e.printStackTrace();
            invStatusLabel.setText("Database Error: Check console.");
            invStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void addEmployeeToDB() {
        String name = empNameInput.getText().trim();
        String role = empRoleInput.getText().trim();

        if (name.isEmpty() || role.isEmpty()) {
            empStatusLabel.setText("Error: Please fill in all fields.");
            empStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            Connection conn = getConnection();
            
            String sql = "INSERT INTO employees (id, name, role) VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM employees), ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, role);
            
            pstmt.executeUpdate(); 
            conn.close();
            
            empStatusLabel.setText("Success: Added " + name + "!");
            empStatusLabel.setStyle("-fx-text-fill: green;");
            
            empNameInput.clear();
            empRoleInput.clear();
            
            loadSchedule();

        } catch (Exception e) {
            e.printStackTrace();
            empStatusLabel.setText("Database Error: Check console.");
            empStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        dbSetup my = new dbSetup();
        return DriverManager.getConnection(DB_URL, my.user, my.pswd);
    }

}