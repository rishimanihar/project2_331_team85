package pos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DatabaseApp extends Application {
    @Override
    public void start(Stage managerStage) throws Exception {

        FXMLLoader managerLoader = new FXMLLoader(getClass().getResource("/manager-view.fxml"));
        Scene managerScene = new Scene(managerLoader.load(), 600, 400);
        managerStage.setTitle("POS System - Manager View");
        managerStage.setScene(managerScene);
        
        managerStage.setX(100); 
        managerStage.setY(100);
        managerStage.show();

        Stage cashierStage = new Stage();

        FXMLLoader cashierLoader = new FXMLLoader(getClass().getResource("/cashier-view.fxml"));
        Scene cashierScene = new Scene(cashierLoader.load(), 600, 400);
        cashierStage.setTitle("POS System - Cashier View");
        cashierStage.setScene(cashierScene);
        
        cashierStage.setX(750); 
        cashierStage.setY(100);
        cashierStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}