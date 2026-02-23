import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ManagerController {

    @FXML private TableView<Product> inventoryTable;
    @FXML private TextField newNameField;
    @FXML private TextField newPriceField;
    @FXML private Label statusLabel;
    @FXML private Button closeButton;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";

    @FXML
    public void initialize() {
        loadInventoryData();
    }

    private void loadInventoryData() {
        ObservableList<Product> productList = FXCollections.observableArrayList();
        dbSetup my = new dbSetup();

        try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT product_name, price FROM products_aayush")) { // 

            while (rs.next()) {
                String name = rs.getString("product_name");
                int price = rs.getInt("price");
                productList.add(new Product(name, price));
            }
            inventoryTable.setItems(productList);
            statusLabel.setText("Inventory loaded successfully.");
            statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);

        } catch (Exception e) {
            statusLabel.setText("Error loading inventory: " + e.getMessage());
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduct() {
        String name = newNameField.getText();
        String priceText = newPriceField.getText();

        if (name.isEmpty() || priceText.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            int price = Integer.parseInt(priceText);
            dbSetup my = new dbSetup();

            // Using PreparedStatement to prevent SQL injection
            String sql = "INSERT INTO products_aayush (product_name, price) VALUES (?, ?)"; // 

            try (Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, name);
                pstmt.setInt(2, price);
                pstmt.executeUpdate();

                // Clear fields and reload table to show the new item
                newNameField.clear();
                newPriceField.clear();
                loadInventoryData(); 
                
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Price must be a valid number.");
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
        } catch (Exception e) {
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            e.printStackTrace();
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}