
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
    @FXML private ListView<String> employeeList;
    @FXML private TextField employeeNameField;

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
        loadEmployees();

        if (queryButton != null) {
            queryButton.setOnAction(event -> loadMenuButtons());
        }
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

    private void loadEmployees() {
        employeeList.getItems().clear(); 
        String sql = "SELECT name FROM employees ORDER BY name ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                employeeList.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("[SQL ERROR] Failed to load employees: " + e.getMessage());
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

    @FXML
    public void handleCustomizationClick(javafx.event.ActionEvent event) {
        if (cartData.isEmpty()) {
            System.out.println("[WARNING] Please select a drink before customizing.");
            return; 
        }

        Button clickedButton = (Button) event.getSource();
        String customValue = clickedButton.getText();

        String prefix = "  - ";
        if (customValue.contains("%")) {
            prefix += "Sugar: ";
        } else {
            prefix += "Ice: ";
        }

        Product modifier = new Product(prefix + customValue, 0.00);
        cartData.add(modifier);
        
        System.out.println("[LOG] Added customization: " + prefix + customValue);
    }

    @FXML
    public void handleAddEmployee(javafx.event.ActionEvent event) {
        String newEmployee = employeeNameField.getText();

        if (newEmployee != null && !newEmployee.trim().isEmpty()) {
            String sql = "INSERT INTO employees (name) VALUES (?)";

            try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, newEmployee.trim());
                pstmt.executeUpdate(); // Executes the insert

                // Update the GUI only if the database insert was successful
                employeeList.getItems().add(newEmployee.trim());
                employeeNameField.clear(); // Empties the text box
                
                System.out.println("[LOG] Successfully added: " + newEmployee);

            } catch (SQLException e) {
                System.err.println("[SQL ERROR] Could not add employee: " + e.getMessage());
            }
        } else {
            System.out.println("[WARNING] Employee name cannot be blank.");
        }
    }

    // --- REMOVE EMPLOYEE ---
    @FXML
    public void handleRemoveEmployee(javafx.event.ActionEvent event) {
        // Find exactly which name the manager clicked on in the list
        String selectedEmployee = employeeList.getSelectionModel().getSelectedItem();

        if (selectedEmployee != null) {
            // SQL DELETE command
            String sql = "DELETE FROM employees WHERE name = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, selectedEmployee);
                int rowsAffected = pstmt.executeUpdate(); // Executes the delete

                if (rowsAffected > 0) {
                    // Update the GUI only if the database delete was successful
                    employeeList.getItems().remove(selectedEmployee);
                    System.out.println("[LOG] Successfully removed: " + selectedEmployee);
                }

            } catch (SQLException e) {
                System.err.println("[SQL ERROR] Could not remove employee: " + e.getMessage());
            }
        } else {
            System.out.println("[WARNING] Please click an employee in the list to remove them.");
        }
    }
}
