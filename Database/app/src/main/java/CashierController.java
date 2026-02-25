import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

public class CashierController {

    @FXML private FlowPane menuGrid;
    @FXML private ListView<String> cartList;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;
    @FXML private Button checkoutBtn;
    @FXML private Button clearBtn;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";
    
    private List<Integer> cartItemIds = new ArrayList<>();
    private double currentTotal = 0.0;

    @FXML
    public void initialize() {
        loadMenuButtons();
        checkoutBtn.setOnAction(e -> checkoutOrder());
        clearBtn.setOnAction(e -> clearCart());
    }
    @FXML
    public void handleCustomizationClick(ActionEvent event) {
        System.out.println("Customization button clicked!");
    }
    private void loadMenuButtons() {
        menuGrid.getChildren().clear();

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, item_name, price FROM menu ORDER BY id ASC");
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("item_name");
                double price = rs.getDouble("price");
                
                Button btn = new Button(name + "\n$" + String.format("%.2f", price));
                btn.setPrefSize(130, 80);
                btn.setStyle("-fx-font-size: 14px; -fx-background-color: #e0e0e0; -fx-border-color: #a0a0a0;");
                
                btn.setOnAction(e -> addToCart(id, name, price));
                menuGrid.getChildren().add(btn);
            }
        } catch (Exception e) {
            System.out.println("[LOG] Error loading menu: " + e.getMessage());
        }
    }

    private void addToCart(int id, String name, double price) {
        cartItemIds.add(id);
        cartList.getItems().add(name + " - $" + String.format("%.2f", price));
        
        currentTotal += price;
        totalLabel.setText("$" + String.format("%.2f", currentTotal));
        statusLabel.setText("Item added.");
    }

    private void clearCart() {
        cartItemIds.clear();
        cartList.getItems().clear();
        currentTotal = 0.0;
        totalLabel.setText("$0.00");
        statusLabel.setText("Order cleared.");
    }

    private void checkoutOrder() {
        if (cartItemIds.isEmpty()) {
            statusLabel.setText("Cannot checkout an empty order!");
            return;
        }

        try (Connection conn = getConnection()) {
            String insertOrderSql = "INSERT INTO orders (order_timestamp, total_amount) VALUES (NOW(), ?) RETURNING id";
            PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql);
            orderStmt.setDouble(1, currentTotal);
            ResultSet rs = orderStmt.executeQuery();
            
            if (rs.next()) {
                int newOrderId = rs.getInt("id");

                String insertItemSql = "INSERT INTO order_items (order_id, menu_id) VALUES (?, ?)";
                PreparedStatement itemStmt = conn.prepareStatement(insertItemSql);
                
                for (Integer menuId : cartItemIds) {
                    itemStmt.setInt(1, newOrderId);
                    itemStmt.setInt(2, menuId);
                    itemStmt.addBatch(); 
                }
                itemStmt.executeBatch();
                
                System.out.println("[LOG] Successfully processed order #" + newOrderId + " for $" + currentTotal);
                statusLabel.setText("Success! Order #" + newOrderId + " saved to DB.");
                
                cartItemIds.clear();
                cartList.getItems().clear();
                currentTotal = 0.0;
                totalLabel.setText("$0.00");
            }
        } catch (Exception e) {
            System.out.println("[LOG] Database Checkout Error: " + e.getMessage());
            statusLabel.setText("Error saving order to database.");
        }
    }

    private Connection getConnection() throws SQLException {
        dbSetup my = new dbSetup();
        return DriverManager.getConnection(DB_URL, my.user, my.pswd);
    }
}