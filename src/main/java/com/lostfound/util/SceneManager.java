package com.lostfound.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public final class SceneManager {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 820;
    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void initialize(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(720);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void switchScene(String fxmlPath, String title) {
        switchScene(fxmlPath, title, null);
    }

    public static void switchScene(String fxmlPath, String title, Consumer<Object> controllerConfigurer) {
        try {
            URL resource = SceneManager.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IllegalArgumentException("FXML file not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            if (controllerConfigurer != null) {
                controllerConfigurer.accept(loader.getController());
            }

            double width = primaryStage.getScene() == null ? DEFAULT_WIDTH : primaryStage.getScene().getWidth();
            double height = primaryStage.getScene() == null ? DEFAULT_HEIGHT : primaryStage.getScene().getHeight();

            Scene scene = new Scene(root, width, height);
            String stylesheet = SceneManager.class.getResource("/styles/style.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            if (!primaryStage.isShowing()) {
                primaryStage.centerOnScreen();
            }
            primaryStage.show();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load screen: " + fxmlPath, exception);
        }
    }
}
