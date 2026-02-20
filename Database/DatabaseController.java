import java.sql.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class DatabaseController {
    @FXML private Button queryButton;
    @FXML private TextArea resultArea;
    @FXML private Button closeButton;
    
    // Updated URL from your Swing code
    private static final String DB_URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu/team_85_db";

    @FXML
    public void initialize() {
        queryButton.setOnAction(event -> runQuery());
        closeButton.setOnAction(event -> closeWindow());
    }

    private void runQuery() {
        resultArea.setText("Connecting to database...");
        StringBuilder results = new StringBuilder();

        try {
            dbSetup my = new dbSetup();
            Connection conn = DriverManager.getConnection(DB_URL, my.user, my.pswd);
            Statement stmt = conn.createStatement();
            
            // Step 2 from your TODO: Relevant query
            String sqlStatement = "SELECT item_name FROM menu";
            ResultSet rs = stmt.executeQuery(sqlStatement);

            while (rs.next()) {
                // Retrieves data from the "item_name" column
                results.append(rs.getString("item_name")).append("\n");
            }

            resultArea.setText(results.toString());

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            resultArea.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeWindow() { 
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}