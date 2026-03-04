import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ManagerDashboardController {

    // Employee UI
    @FXML private ListView<String> scheduleList;
    @FXML private TextField empNameInput;
    @FXML private TextField empRoleInput;
    @FXML private Button addEmpBtn;
    @FXML private Label empStatusLabel;

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

    // Trends & Overview UI
    @FXML private LineChart<String, Number> trendsChart;
    @FXML private Label revenueLabel;
    @FXML private Label ordersLabel;

    // Chat UI
    @FXML private ListView<String> chatList;
    @FXML private TextField chatInput;
    @FXML private Button sendButton;

    // Menu Management UI
    @FXML private TableView<Models.MenuItem> menuTable;
    @FXML private TableColumn<Models.MenuItem, Integer> menuIdCol;
    @FXML private TableColumn<Models.MenuItem, String> menuNameCol;
    @FXML private TableColumn<Models.MenuItem, Double> menuPriceCol;
    @FXML private TextField menuNameInput;
    @FXML private TextField menuPriceInput;
    @FXML private Button addMenuBtn;
    @FXML private Label menuStatusLabel;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";

    @FXML
    public void initialize() {
        // Init Tables
        invNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        invQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        invUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        menuIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        menuNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        menuPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        loadInventory();
        loadItemTrends();
        loadQuarterlyOverview();
        loadSchedule();
        loadMenuData();
        
        chatList.getItems().add("System: Welcome to the Manager Dashboard.");
        sendButton.setOnAction(e -> sendMessage());
        chatInput.setOnAction(e -> sendMessage()); 

        addMenuBtn.setOnAction(e -> addMenuItemToDB());
        addInvBtn.setOnAction(e -> addInventoryItemToDB());
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
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, item_name, quantity, unit FROM inventory");
            while (rs.next()) {
                invList.add(new Models.InventoryItem(rs.getInt("id"), rs.getString("item_name"), rs.getInt("quantity"), rs.getString("unit")));
            }
            inventoryTable.setItems(invList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadItemTrends() {
        trendsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales (Last 14 Days)");
        
        String sql = "SELECT TRIM(to_char(order_time, 'Day')) AS day_of_week, SUM(total_price) AS total_revenue " +
                    "FROM orders GROUP BY day_of_week, EXTRACT(ISODOW FROM order_time) ORDER BY EXTRACT(ISODOW FROM order_time)";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<XYChart.Data<String, Number>> dataPoints = new ArrayList<>();
            while (rs.next()) {
                dataPoints.add(new XYChart.Data<>(rs.getString("day_of_week"), rs.getDouble("total_revenue")));
            }
            Collections.reverse(dataPoints);
            series.getData().addAll(dataPoints);
            trendsChart.getData().add(series);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadQuarterlyOverview() {
        String sql = "SELECT COUNT(order_id) as total_orders, SUM(total_price) as total_rev FROM orders WHERE order_time >= NOW() - INTERVAL '3 months'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                ordersLabel.setText(String.valueOf(rs.getInt("total_orders")));
                revenueLabel.setText(String.format("$%.2f", rs.getDouble("total_rev")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadSchedule() {
        scheduleList.getItems().clear();
        List<String> employeeNames = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT name FROM employees");
            while (rs.next()) {
                employeeNames.add(rs.getString("name"));
            }
        } catch (Exception e) { e.printStackTrace(); }

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
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, item_name, price FROM menu ORDER BY id ASC");
            while (rs.next()) {
                menuList.add(new Models.MenuItem(rs.getInt("id"), rs.getString("item_name"), rs.getDouble("price")));
            }
            menuTable.setItems(menuList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- DB INSERTS ---
    private void addMenuItemToDB() {
        String name = menuNameInput.getText().trim();
        String priceText = menuPriceInput.getText().trim();
        if (name.isEmpty() || priceText.isEmpty()) { menuStatusLabel.setText("Error: Fill all fields."); return; }

        try (Connection conn = getConnection()) {
            double price = Double.parseDouble(priceText);
            String sql = "INSERT INTO menu (id, item_name, price) VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM menu), ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.executeUpdate(); 
            menuStatusLabel.setText("Added " + name + "!");
            menuNameInput.clear(); menuPriceInput.clear();
            loadMenuData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addInventoryItemToDB() {
        String name = invNameInput.getText().trim();
        String qtyText = invQtyInput.getText().trim();
        String unit = invUnitInput.getText().trim();
        if (name.isEmpty() || qtyText.isEmpty() || unit.isEmpty()) { invStatusLabel.setText("Error: Fill all fields."); return; }

        try (Connection conn = getConnection()) {
            int qty = Integer.parseInt(qtyText);
            String sql = "INSERT INTO inventory (id, item_name, quantity, unit) VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM inventory), ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, qty);
            pstmt.setString(3, unit);
            pstmt.executeUpdate(); 
            invStatusLabel.setText("Added " + name + "!");
            invNameInput.clear(); invQtyInput.clear(); invUnitInput.clear();
            loadInventory();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addEmployeeToDB() {
        String name = empNameInput.getText().trim();
        String role = empRoleInput.getText().trim();
        if (name.isEmpty() || role.isEmpty()) { empStatusLabel.setText("Error: Fill all fields."); return; }

        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO employees (id, name, role) VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM employees), ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, role);
            pstmt.executeUpdate(); 
            empStatusLabel.setText("Added " + name + "!");
            empNameInput.clear(); empRoleInput.clear();
            loadSchedule();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Connection getConnection() throws SQLException {
        try { Class.forName("org.postgresql.Driver"); } catch (Exception e) {}
        dbSetup my = new dbSetup();
        return DriverManager.getConnection(DB_URL, my.user, my.pswd);
    }
}