import java.sql.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;     // Added
import javafx.scene.control.TableView; // Added
import javafx.stage.Stage;

public class DatabaseController {
    @FXML private Button queryButton;
    @FXML private Button closeButton;
    
    // Changed from resultArea to menuGrid to match FXML fx:id
    @FXML private TextArea menuGrid; 

    // Added missing fields from FXML
    @FXML private TableView<?> cartList; 
    @FXML private Label totalLabel;

    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";

    @FXML
    public void initialize() {
        queryButton.setOnAction(event -> runQuery());
        closeButton.setOnAction(event -> closeWindow());
    }

    private void runQuery() {
        menuGrid.setText("Connecting to database..."); // Updated name
        StringBuilder results = new StringBuilder();

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();

            String sqlStatement = "SELECT item_name FROM menu";
            ResultSet rs = stmt.executeQuery(sqlStatement);

            while (rs.next()) {
                results.append(rs.getString("item_name")).append("\n");
            }

            menuGrid.setText(results.toString()); // Updated name

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            menuGrid.setText("Error: " + e.getMessage()); // Updated name
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
