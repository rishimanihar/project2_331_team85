import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the POS System application.
 * This class extends JavaFX Application and handles the initialization
 * of the primary stages (windows) for both the Manager and Cashier views.
 */
public class DatabaseApp extends Application {

    /**
     * Starts the JavaFX application.
     * Loads the FXML layouts for the Manager and Cashier dashboards,
     * sets up their respective scenes, and displays the windows.
     * 
     * @param managerStage The primary stage provided by the JavaFX runtime, used for the Manager view.
     * @throws Exception If FXML files cannot be loaded.
     */
    @Override
    public void start(Stage managerStage) throws Exception {

        FXMLLoader managerLoader = new FXMLLoader(getClass().getResource("manager-view.fxml"));
        Scene managerScene = new Scene(managerLoader.load(), 600, 400);
        managerStage.setTitle("POS System - Manager View");
        managerStage.setScene(managerScene);
        
        managerStage.setX(100); 
        managerStage.setY(100);
        managerStage.show();

        Stage cashierStage = new Stage();

        FXMLLoader cashierLoader = new FXMLLoader(getClass().getResource("cashier-view.fxml"));
        Scene cashierScene = new Scene(cashierLoader.load(), 600, 400);
        cashierStage.setTitle("POS System - Cashier View");
        cashierStage.setScene(cashierScene);
        
        cashierStage.setX(750); 
        cashierStage.setY(100);
        cashierStage.show();
    }

    /**
     * Main method to launch the application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}