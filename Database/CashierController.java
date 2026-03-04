import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

/**
 * Controller class for the Cashier View.
 * Handles user interactions for taking orders, customizing items,
 * and submitting transactions to the database.
 */
public class CashierController {

    // FXML injected UI components
    @FXML private FlowPane buttonContainer;
    @FXML private ListView<String> cartList;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;
    @FXML private Button submitOrderButton;
    @FXML private Button clearBtn;
    @FXML private Button queryButton;
    
    // Customization buttons
    @FXML private Button btnSweet0;
    @FXML private Button btnSweet50;
    @FXML private Button btnSweet100;
    @FXML private Button btnSweet120;
    @FXML private Button btnIceNone;
    @FXML private Button btnIceLess;
    @FXML private Button btnIceReg;
    @FXML private Button btnIceExtra;

    // Database connection URL
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";
    
    // State variables for the current order
    private List<Integer> cartItemIds = new ArrayList<>();
    private double currentTotal = 0.0;
    private String currentSweetness = "100%";
    private String currentIceLevel = "Regular";

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    public void initialize() {
        loadMenuButtons();
        submitOrderButton.setOnAction(e -> checkoutOrder());
        clearBtn.setOnAction(e -> clearCart());
        queryButton.setOnAction(e -> {
            loadMenuButtons();
            statusLabel.setText("Menu refreshed!");
        });
        
        // Setup customization button handlers
        setupCustomizationButtons();
    }

    /**
     * Sets up event handlers for sweetness and ice level customization buttons.
     */
    private void setupCustomizationButtons() {
        // Sweetness buttons
        btnSweet0.setOnAction(e -> setSweetness("0%"));
        btnSweet50.setOnAction(e -> setSweetness("50%"));
        btnSweet100.setOnAction(e -> setSweetness("100%"));
        btnSweet120.setOnAction(e -> setSweetness("120%"));
        
        // Ice buttons
        btnIceNone.setOnAction(e -> setIceLevel("None"));
        btnIceLess.setOnAction(e -> setIceLevel("Less"));
        btnIceReg.setOnAction(e -> setIceLevel("Regular"));
        btnIceExtra.setOnAction(e -> setIceLevel("Extra"));
    }
    
    /**
     * Updates the current sweetness level selection.
     * @param sweetness The sweetness level string (e.g., "50%")
     */
    private void setSweetness(String sweetness) {
        currentSweetness = sweetness;
        statusLabel.setText("Sweetness set to: " + sweetness);
    }
    
    /**
     * Updates the current ice level selection.
     * @param iceLevel The ice level string (e.g., "Less")
     */
    private void setIceLevel(String iceLevel) {
        currentIceLevel = iceLevel;
        statusLabel.setText("Ice level set to: " + iceLevel);
    }

    /**
     * Fetches menu items from the database and dynamically creates buttons for them.
     */
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

    /**
     * Adds an item to the cart with the currently selected customization options.
     * @param id The database ID of the menu item
     * @param name The name of the menu item
     * @param price The price of the menu item
     */
    private void addToCart(int id, String name, double price) {
        cartItemIds.add(id);
        
        // Add main item
        String displayItem = name + " - $" + String.format("%.2f", price);
        cartList.getItems().add(displayItem);
        
        // Add customization info on separate lines if not default
        if (!currentSweetness.equals("100%")) {
            cartList.getItems().add("    Sweetness: " + currentSweetness);
        }
        if (!currentIceLevel.equals("Regular")) {
            cartList.getItems().add("    Ice: " + currentIceLevel);
        }
        
        // Always show a separator line for readability
        cartList.getItems().add("");
        
        currentTotal += price;
        totalLabel.setText("Total: $" + String.format("%.2f", currentTotal));
        statusLabel.setText("Item added.");
        
        // Reset customization to defaults after adding to cart
        currentSweetness = "100%";
        currentIceLevel = "Regular";
    }

    /**
     * Clears the current order cart and resets totals and customizations.
     */
    private void clearCart() {
        cartItemIds.clear();
        cartList.getItems().clear();
        currentTotal = 0.0;
        totalLabel.setText("Total: $0.00");
        statusLabel.setText("Order cleared.");
        
        // Reset customization to defaults
        currentSweetness = "100%";
        currentIceLevel = "Regular";
    }

    /**
     * Submits the current order to the database.
     * Inserts into 'orders' table first, then 'order_items'.
     */
    private void checkoutOrder() {
        if (cartItemIds.isEmpty()) {
            statusLabel.setText("Cannot checkout empty order!");
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
                statusLabel.setText("Success! Order #" + newOrderId + " saved.");
                
                cartItemIds.clear();
                cartList.getItems().clear();
                currentTotal = 0.0;
                totalLabel.setText("Total: $0.00");
            }
        } catch (Exception e) {
            System.out.println("[LOG] Database Checkout Error: " + e.getMessage());
            statusLabel.setText("Error saving order.");
        }
    }

    /**
     * Establishes a connection to the database using credentials from dbSetup.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    private Connection getConnection() throws SQLException {
        try { Class.forName("org.postgresql.Driver"); } catch (Exception e) {}
        dbSetup my = new dbSetup();
        return DriverManager.getConnection(DB_URL, my.user, my.pswd);
    }
}