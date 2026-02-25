import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ManagerDashboardController {

    // --- YOUR CASHIER SIDE ---
    @FXML private FlowPane buttonContainer;
    @FXML private TableView<Product> cartList;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private Label totalLabel;
    @FXML private Button queryButton;
    @FXML private Button submitOrderButton;

    // --- TEAMMATE'S MANAGER DASHBOARD ---
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> invNameCol;
    @FXML private TableColumn<InventoryItem, Integer> invQtyCol;
    @FXML private TableColumn<InventoryItem, String> invUnitCol;
    
    @FXML private LineChart<String, Number> trendsChart;
    @FXML private Label revenueLabel;
    @FXML private Label ordersLabel;
    @FXML private ListView<String> scheduleList;

    // --- TEAMMATE'S MENU MANAGEMENT ---
    @FXML private TableView<Product> menuTable; // Replaced MenuItem with Product for consistency
    @FXML private TableColumn<Product, Integer> menuIdCol;
    @FXML private TableColumn<Product, String> menuNameCol;
    @FXML private TableColumn<Product, Double> menuPriceCol;
    @FXML private TextField menuNameInput;
    @FXML private TextField menuPriceInput;
    @FXML private Button addMenuBtn;
    @FXML private Label menuStatusLabel;

    // --- TEAMMATE'S CHAT ---
    @FXML private ListView<String> chatList;
    @FXML private TextField chatInput;
    @FXML private Button sendButton;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";
    private ObservableList<Product> cartData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Setup Cashier Table (Your side)
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        cartList.setItems(cartData);

        // 2. Setup Manager Tables (Teammate's side)
        invNameCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        invQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        invUnitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        menuIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        menuNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        menuPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        // 3. Load All Data
        loadInventory();
        loadItemTrends();
        loadQuarterlyOverview();
        loadMenuButtons(); // Your grid buttons
        loadMenuTable();   // Manager's data table
        loadSchedule();

        // 4. Set Event Handlers
        if (queryButton != null) queryButton.setOnAction(event -> loadMenuButtons());
        if (sendButton != null) sendButton.setOnAction(e -> sendMessage());
        if (addMenuBtn != null) addMenuBtn.setOnAction(e -> addMenuItemToDB());
        
        chatList.getItems().add("System: Welcome to the Manager Dashboard.");
    }

    // --- CASHIER LOGIC ---
    private void loadMenuButtons() {
        buttonContainer.getChildren().clear();
        String sql = "SELECT id, item_name, price FROM menu";
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("item_name");
                double price = rs.getDouble("price");
                Button itemButton = new Button(name + "\n$" + String.format("%.2f", price));
                itemButton.setPrefSize(120, 80);
                itemButton.setOnAction(e -> {
                    cartData.add(new Product(id, name, price));
                    updateTotal();
                });
                buttonContainer.getChildren().add(itemButton);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateTotal() {
        double total = cartData.stream().mapToDouble(Product::getPrice).sum();
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    @FXML
    private void handleSubmitOrder() {
        if (cartData.isEmpty()) return;
        int nextId = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(order_id), 0) + 1 FROM orders")) {
            if (rs.next()) nextId = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); return; }

        String orderSql = "INSERT INTO orders (order_id, order_time, total_price) VALUES (?, CURRENT_TIMESTAMP, ?)";
        String itemSql = "INSERT INTO order_items (order_id, menu_id) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd)) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtOrder = conn.prepareStatement(orderSql);
                 PreparedStatement pstmtItem = conn.prepareStatement(itemSql)) {
                double total = cartData.stream().mapToDouble(Product::getPrice).sum();
                pstmtOrder.setInt(1, nextId);
                pstmtOrder.setDouble(2, total);
                pstmtOrder.executeUpdate();
                for (Product p : cartData) {
                    if (p.getId() > 0) {
                        pstmtItem.setInt(1, nextId);
                        pstmtItem.setInt(2, p.getId());
                        pstmtItem.addBatch();
                    }
                }
                pstmtItem.executeBatch();
                conn.commit();
                cartData.clear();
                updateTotal();
                loadQuarterlyOverview();
                System.out.println("[DEMO] Order #" + nextId + " submitted.");
            } catch (SQLException e) { conn.rollback(); e.printStackTrace(); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- MANAGER LOGIC (REPORTS & INVENTORY) ---
    private void loadInventory() {
        ObservableList<InventoryItem> invList = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT item_name, quantity, unit FROM inventory")) {
            while (rs.next()) {
                invList.add(new InventoryItem(rs.getString("item_name"), rs.getInt("quantity"), rs.getString("unit")));
            }
            inventoryTable.setItems(invList);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadItemTrends() {
        trendsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Sales (Last 14 Days)");
        String sql = "SELECT TRIM(to_char(order_time, 'Day')) AS day_of_week, SUM(total_price) AS total_revenue " +
                     "FROM orders GROUP BY day_of_week, EXTRACT(ISODOW FROM order_time) " +
                     "ORDER BY EXTRACT(ISODOW FROM order_time)";
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("day_of_week"), rs.getDouble("total_revenue")));
            }
            trendsChart.getData().add(series);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadQuarterlyOverview() {
        String sql = "SELECT COUNT(order_id) as total_orders, SUM(total_price) as total_rev FROM orders WHERE order_time >= NOW() - INTERVAL '3 months'";
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                ordersLabel.setText(String.valueOf(rs.getInt("total_orders")));
                revenueLabel.setText(String.format("%.2f", rs.getDouble("total_rev")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- MANAGER LOGIC (MENU & EMPLOYEES) ---
    private void loadMenuTable() {
        ObservableList<Product> menuList = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, item_name, price FROM menu ORDER BY id ASC")) {
            while (rs.next()) {
                menuList.add(new Product(rs.getInt("id"), rs.getString("item_name"), rs.getDouble("price")));
            }
            menuTable.setItems(menuList);
        } catch (SQLException e) { e.printStackTrace(); }
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
        
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd)) {
            // Manually calculate the next ID to satisfy the NOT NULL constraint
            int nextId = 1;
            String idSql = "SELECT COALESCE(MAX(id), 0) + 1 FROM menu";
            try (Statement idStmt = conn.createStatement();
                 ResultSet rs = idStmt.executeQuery(idSql)) {
                if (rs.next()) {
                    nextId = rs.getInt(1);
                }
            }

            // Insert using the calculated ID
            String sql = "INSERT INTO menu (id, item_name, price) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, nextId);
                pstmt.setString(2, name);
                pstmt.setDouble(3, price);
                pstmt.executeUpdate(); 
            }
            
            menuStatusLabel.setText("Success: Added " + name + " (ID: " + nextId + ")");
            menuNameInput.clear();
            menuPriceInput.clear();
            
            // Refresh both the Manager table and the Cashier buttons
            loadMenuTable();
            loadMenuButtons();
        }
    } catch (NumberFormatException nfe) {
        menuStatusLabel.setText("Error: Price must be a number.");
    } catch (Exception e) {
        e.printStackTrace();
        menuStatusLabel.setText("Database Error: Check console.");
    }
}
    private void loadSchedule() {
        scheduleList.getItems().clear();
        List<String> employeeNames = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM employees")) {
            while (rs.next()) employeeNames.add(rs.getString("name"));
        } catch (SQLException e) { e.printStackTrace(); }
        
        if (employeeNames.isEmpty()) return;
        String[] days = {"Mon", "Tue", "Wed", "Thr", "Fri", "Sat", "Sun"};
        Random rand = new Random();
        for (String day : days) {
            String mEmp = employeeNames.get(rand.nextInt(employeeNames.size()));
            String eEmp = employeeNames.get(rand.nextInt(employeeNames.size()));
            scheduleList.getItems().add(day + ": " + mEmp + " (M) | " + eEmp + " (E)");
        }
    }

    private void sendMessage() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty()) { chatList.getItems().add("Manager: " + msg); chatInput.clear(); }
    }

    @FXML
    public void handleCustomizationClick(javafx.event.ActionEvent event) {
        if (cartData.isEmpty()) return;
        Button btn = (Button) event.getSource();
        String val = btn.getText();
        String pref = val.contains("%") ? "  - Sugar: " : "  - Ice: ";
        cartData.add(new Product(0, pref + val, 0.00));
    }
}
