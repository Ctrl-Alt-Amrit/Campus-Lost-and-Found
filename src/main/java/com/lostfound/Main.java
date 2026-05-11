package com.lostfound;

import com.lostfound.util.AlertUtil;
import com.lostfound.util.DatabaseMigrator;
import com.lostfound.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.initialize(primaryStage);
        try {
            DatabaseMigrator.migrate();
        } catch (Exception exception) {
            AlertUtil.showError("Database Upgrade Failed", exception.getMessage());
        }
        SceneManager.switchScene("/view/login.fxml", "Campus Lost and Found System");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
