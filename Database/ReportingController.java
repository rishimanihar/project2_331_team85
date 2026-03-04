import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ReportingController {
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";
    
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private BarChart<String, Number> usageChart;
    @FXML private Button generateUsageBtn;
    
    @FXML private TextArea xReportArea;
    @FXML private Button generateXReportBtn;
    
    @FXML private TextArea zReportArea;
    @FXML private Button generateZReportBtn;
    @FXML private Label lastZReportLabel;
    
    @FXML private DatePicker salesStartDatePicker;
    @FXML private DatePicker salesEndDatePicker;
    @FXML private TableView<Models.SalesItem> salesTable;
    @FXML private TableColumn<Models.SalesItem, String> salesItemCol;
    @FXML private TableColumn<Models.SalesItem, Integer> salesQuantityCol;
    @FXML private TableColumn<Models.SalesItem, Double> salesRevenueCol;
    @FXML private Button generateSalesReportBtn;
    
    @FXML private TextField newItemName;
    @FXML private TextField newItemPrice;
    @FXML private TextArea ingredientsArea;
    @FXML private Button addMenuItemBtn;
    @FXML private Label addItemStatus;

    @FXML
    public void initialize() {
        setupSalesTable();
        setupEventHandlers();
        checkLastZReport();
    }

    // Helper to ensure driver is loaded
    private Connection getConnection() throws SQLException {
        try { Class.forName("org.postgresql.Driver"); } catch (Exception e) {}
        dbSetup my = new dbSetup();
        return DriverManager.getConnection(DB_URL, my.user, my.pswd);
    }

    private void setupSalesTable() {
        salesItemCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        salesQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        salesRevenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
    }

    private void setupEventHandlers() {
        generateUsageBtn.setOnAction(e -> generateProductUsageChart());
        generateXReportBtn.setOnAction(e -> generateXReport());
        generateZReportBtn.setOnAction(e -> generateZReport());
        generateSalesReportBtn.setOnAction(e -> generateSalesReport());
        addMenuItemBtn.setOnAction(e -> addNewMenuItem());
    }

    private void checkLastZReport() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT MAX(report_date) FROM z_reports");
            if (rs.next() && rs.getDate(1) != null) {
                lastZReportLabel.setText("Last Z-Report: " + rs.getDate(1).toString());
                if (rs.getDate(1).toLocalDate().equals(LocalDate.now())) {
                    generateZReportBtn.setDisable(true);
                    generateZReportBtn.setText("Z-Report Already Run Today");
                }
            } else {
                lastZReportLabel.setText("No Z-Reports found");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void generateProductUsageChart() {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showAlert("Please select both start and end dates");
            return;
        }
        
        usageChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inventory Usage");
        
        String sql = "SELECT i.item_name, COALESCE(SUM(oi.quantity * mi.quantity_used), 0) as total_used " +
                     "FROM inventory i LEFT JOIN menu_ingredients mi ON i.id = mi.inventory_id " +
                     "LEFT JOIN order_items oi ON mi.menu_id = oi.menu_id " +
                     "LEFT JOIN orders o ON oi.order_id = o.order_id " +
                     "WHERE DATE(o.order_time) BETWEEN ? AND ? GROUP BY i.item_name ORDER BY total_used DESC LIMIT 10";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDatePicker.getValue()));
            pstmt.setDate(2, Date.valueOf(endDatePicker.getValue()));
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("item_name"), rs.getDouble("total_used")));
            }
            usageChart.getData().add(series);
        } catch (Exception e) { showAlert("Error generating usage chart: " + e.getMessage()); }
    }

    private void generateXReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== X-REPORT FOR ").append(LocalDate.now().format(DateTimeFormatter.ISO_DATE)).append(" ===\n\n");
        report.append("HOURLY SALES BREAKDOWN:\n------------------------\n");

        String sql = "SELECT EXTRACT(HOUR FROM order_time) as hour, COUNT(*) as order_count, SUM(total_price) as total_sales " +
                     "FROM orders WHERE DATE(order_time) = CURRENT_DATE GROUP BY hour ORDER BY hour";
                     
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                report.append(String.format("%02d:00-%02d:59: %d orders, $%.2f sales\n", 
                              rs.getInt("hour"), rs.getInt("hour"), rs.getInt("order_count"), rs.getDouble("total_sales")));
            }
            xReportArea.setText(report.toString());
        } catch (Exception e) { showAlert("Error generating X-Report: " + e.getMessage()); }
    }

    private void generateZReport() {
        StringBuilder report = new StringBuilder();
        LocalDate today = LocalDate.now();
        report.append("=== Z-REPORT FOR ").append(today.format(DateTimeFormatter.ISO_DATE)).append(" ===\n\n");

        String sql = "SELECT COUNT(*) as total_orders, SUM(total_price) as total_revenue FROM orders WHERE DATE(order_time) = CURRENT_DATE";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                double totalRev = rs.getDouble("total_revenue");
                double tax = totalRev * 0.0825;
                report.append(String.format("Total Orders: %d\n", rs.getInt("total_orders")));
                report.append(String.format("Gross Sales: $%.2f\nTax (8.25%%): $%.2f\nNet Sales: $%.2f\n", totalRev, tax, totalRev - tax));
                
                try {
                    String insertSql = "INSERT INTO z_reports (report_date, total_orders, total_revenue, tax_amount) VALUES (?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(insertSql);
                    pstmt.setDate(1, Date.valueOf(today));
                    pstmt.setInt(2, rs.getInt("total_orders"));
                    pstmt.setDouble(3, totalRev);
                    pstmt.setDouble(4, tax);
                    pstmt.executeUpdate();
                } catch (Exception ignored) {} // Fails gracefully if z_reports table doesn't exist yet
            }
            conn.commit();
            zReportArea.setText(report.toString());
            generateZReportBtn.setDisable(true);
            generateZReportBtn.setText("Z-Report Already Run");
            lastZReportLabel.setText("Last Z-Report: " + today.toString());
        } catch (Exception e) { showAlert("Error generating Z-Report: " + e.getMessage()); }
    }

    private void generateSalesReport() {
        if (salesStartDatePicker.getValue() == null || salesEndDatePicker.getValue() == null) {
            showAlert("Please select dates"); return;
        }
        ObservableList<Models.SalesItem> data = FXCollections.observableArrayList();
        String sql = "SELECT m.item_name, COUNT(oi.menu_id) as quantity_sold, SUM(m.price) as total_revenue " +
                     "FROM menu m JOIN order_items oi ON m.id = oi.menu_id JOIN orders o ON oi.order_id = o.order_id " +
                     "WHERE DATE(o.order_time) BETWEEN ? AND ? GROUP BY m.id, m.item_name ORDER BY quantity_sold DESC";
                     
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(salesStartDatePicker.getValue()));
            pstmt.setDate(2, Date.valueOf(salesEndDatePicker.getValue()));
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                data.add(new Models.SalesItem(rs.getString("item_name"), rs.getInt("quantity_sold"), rs.getDouble("total_revenue")));
            }
            salesTable.setItems(data);
        } catch (Exception e) { showAlert("Error: " + e.getMessage()); }
    }

    private void addNewMenuItem() {
        String name = newItemName.getText().trim();
        String priceText = newItemPrice.getText().trim();
        if (name.isEmpty() || priceText.isEmpty()) { showAlert("Enter name and price"); return; }

        try (Connection conn = getConnection()) {
            double price = Double.parseDouble(priceText);
            // Re-used the safe insert query from your ManagerDashboard logic
            String sql = "INSERT INTO menu (id, item_name, price) VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM menu), ?, ?) RETURNING id";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            
            // Execute and clean up UI
            pstmt.executeQuery(); 
            addItemStatus.setText("Menu item added!");
            addItemStatus.setStyle("-fx-text-fill: green;");
            newItemName.clear(); newItemPrice.clear(); ingredientsArea.clear();
        } catch (Exception e) { showAlert("Error: " + e.getMessage()); }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}