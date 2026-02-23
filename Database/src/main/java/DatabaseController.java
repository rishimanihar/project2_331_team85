import java.sql.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.layout.FlowPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DatabaseController {
    // --- FXML UI Elements ---
    @FXML private Button queryButton;
    @FXML private Button closeButton;
    @FXML private Button submitOrderButton;
    @FXML private FlowPane buttonContainer;
    @FXML private Label totalLabel;
    
    // The TableView and its columns
    @FXML private TableView<MenuItem> cartList;
    @FXML private TableColumn<MenuItem, String> nameColumn;
    @FXML private TableColumn<MenuItem, Double> priceColumn;

    // --- Database & Data Variables ---
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";
    private ObservableList<MenuItem> cartData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Link TableColumns to the "name" and "price" getters in MenuItem.java
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        // Attach the list to the table
        cartList.setItems(cartData);

        // Set Button Actions
        queryButton.setOnAction(event -> loadMenuButtons());
        closeButton.setOnAction(event -> closeWindow());
        
        // Load menu buttons automatically on startup
        loadMenuButtons();
    }

    private void loadMenuButtons() {
        buttonContainer.getChildren().clear(); 
        String sql = "SELECT item_name, price FROM menu";

        try (Connection conn = DriverManager.getConnection(DB_URL, dbSetup.user, dbSetup.pswd);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("item_name");
                double price = rs.getDouble("price");

                Button itemButton = new Button(name + "\n$" + String.format("%.2f", price));
                itemButton.setPrefSize(120, 80); 
                itemButton.setOnAction(e -> addToCart(name, price));

                buttonContainer.getChildren().add(itemButton);
            }
        } catch (SQLException e) {
            System.err.println("[LOG] Database error: " + e.getMessage());
        }
    }

    private void addToCart(String name, double price) {
        cartData.add(new MenuItem(name, price));
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (MenuItem item : cartData) {
            total += item.getPrice();
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
