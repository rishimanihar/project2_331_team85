import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

public class CashierController {

    // ALL OF THESE NOW MATCH YOUR FXML FILE EXACTLY
    @FXML private FlowPane buttonContainer; 
    @FXML private ListView<String> cartList;
    @FXML private Label totalLabel;
    @FXML private Button submitOrderButton; 
    @FXML private Button queryButton; 

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";
    
    private List<Integer> cartItemIds = new ArrayList<>();
    private double currentTotal = 0.0;

    @FXML
    public void initialize() {
        loadMenuButtons();
        // Hooking up the FXML buttons to their logic
        submitOrderButton.setOnAction(e -> checkoutOrder());
        queryButton.setOnAction(e -> {
        loadMenuButtons();
        System.out.println("Menu manually refreshed from database!");
    });
    }
    
    @FXML
    public void handleCustomizationClick(ActionEvent event) {
        System.out.println("Customization button clicked!");
    }

    private void loadMenuButtons() {
        buttonContainer.getChildren().clear();

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
                buttonContainer.getChildren().add(btn);
            }
        } catch (Exception e) {
            System.out.println("[LOG] Error loading menu: " + e.getMessage());
        }
    }

    private void addToCart(int id, String name, double price) {
        cartItemIds.add(id);
        cartList.getItems().add(name + " - $" + String.format("%.2f", price));
        
        currentTotal += price;
        totalLabel.setText("Total: $" + String.format("%.2f", currentTotal));
    }

    private void clearCart() {
        cartItemIds.clear();
        cartList.getItems().clear();
        currentTotal = 0.0;
        totalLabel.setText("Total: $0.00");
    }

    private void checkoutOrder() {
        if (cartItemIds.isEmpty()) {
            System.out.println("Cannot checkout an empty order!");
            return;
        }

        try (Connection conn = getConnection()) {
            String insertOrderSql = "INSERT INTO orders (order_time, total_price) VALUES (NOW(), ?) RETURNING order_id";
            PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql);
            orderStmt.setDouble(1, currentTotal);
            ResultSet rs = orderStmt.executeQuery();
            
            if (rs.next()) {
                int newOrderId = rs.getInt("order_id");

                String insertItemSql = "INSERT INTO order_items (order_id, menu_id) VALUES (?, ?)";
                PreparedStatement itemStmt = conn.prepareStatement(insertItemSql);
                
                for (Integer menuId : cartItemIds) {
                    itemStmt.setInt(1, newOrderId);
                    itemStmt.setInt(2, menuId);
                    itemStmt.addBatch(); 
                }
                itemStmt.executeBatch();
                
                System.out.println("[LOG] Successfully processed order #" + newOrderId + " for $" + currentTotal);
                
                cartItemIds.clear();
                cartList.getItems().clear();
                currentTotal = 0.0;
                totalLabel.setText("Total: $0.00");
            }
        } catch (Exception e) {
            System.out.println("[LOG] Database Checkout Error: " + e.getMessage());
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