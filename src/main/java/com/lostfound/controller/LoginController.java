package com.lostfound.controller;

import com.lostfound.model.User;
import com.lostfound.service.AuthService;
import com.lostfound.util.AlertUtil;
import com.lostfound.util.SceneManager;
import com.lostfound.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class LoginController {
    private final AuthService authService = new AuthService();

    @FXML
    private TabPane authTabPane;
    @FXML
    private Tab loginTab;
    @FXML
    private TextField loginEmailField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private TextField adminEmailField;
    @FXML
    private PasswordField adminPasswordField;
    @FXML
    private TextField registerNameField;
    @FXML
    private TextField registerEmailField;
    @FXML
    private PasswordField registerPasswordField;
    @FXML
    private PasswordField registerConfirmPasswordField;

    @FXML
    public void initialize() {
        SessionManager.clear();
        registerNameField.setTextFormatter(createTextLimitFormatter(100));
        registerEmailField.setTextFormatter(createTextLimitFormatter(150));
        loginPasswordField.setOnAction(event -> handleLogin());
        adminPasswordField.setOnAction(event -> handleAdminLogin());
        registerConfirmPasswordField.setOnAction(event -> handleRegister());
    }

    @FXML
    private void handleLogin() {
        try {
            User user = authService.login(loginEmailField.getText(), loginPasswordField.getText());
            SessionManager.setCurrentUser(user);
            openHomeFor(user);
        } catch (Exception exception) {
            loginPasswordField.clear();
            AlertUtil.showError("Login Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleAdminLogin() {
        try {
            User user = authService.login(adminEmailField.getText(), adminPasswordField.getText());
            if (!user.isAdmin()) {
                throw new IllegalArgumentException("This account does not have administrator access.");
            }

            SessionManager.setCurrentUser(user);
            SceneManager.switchScene("/view/admin.fxml", "Admin Panel");
        } catch (Exception exception) {
            adminPasswordField.clear();
            AlertUtil.showError("Admin Login Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            authService.register(
                    registerNameField.getText(),
                    registerEmailField.getText(),
                    registerPasswordField.getText(),
                    registerConfirmPasswordField.getText()
            );
            clearRegistrationFields();
            authTabPane.getSelectionModel().select(loginTab);
            AlertUtil.showInfo("Registration Successful", "Your account has been created. Please log in.");
        } catch (Exception exception) {
            AlertUtil.showError("Registration Failed", exception.getMessage());
        }
    }

    private void clearRegistrationFields() {
        registerNameField.clear();
        registerEmailField.clear();
        registerPasswordField.clear();
        registerConfirmPasswordField.clear();
        loginEmailField.requestFocus();
    }

    private void openHomeFor(User user) {
        if (user.isAdmin()) {
            SceneManager.switchScene("/view/admin.fxml", "Admin Panel");
        } else {
            SceneManager.switchScene("/view/dashboard.fxml", "Dashboard");
        }
    }

    private TextFormatter<String> createTextLimitFormatter(int maxLength) {
        return new TextFormatter<>(change ->
                change.getControlNewText().length() <= maxLength ? change : null);
    }
}
