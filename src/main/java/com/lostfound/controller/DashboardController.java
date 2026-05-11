package com.lostfound.controller;

import com.lostfound.model.User;
import com.lostfound.service.ItemService;
import com.lostfound.util.AlertUtil;
import com.lostfound.util.SceneManager;
import com.lostfound.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DashboardController {
    private final ItemService itemService = new ItemService();

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label lostCountLabel;
    @FXML
    private Label foundCountLabel;
    @FXML
    private Label myReportsLabel;
    @FXML
    private Label myClaimsLabel;
    @FXML
    private Button adminButton;

    @FXML
    public void initialize() {
        loadDashboard();
    }

    @FXML
    private void handleReportLost() {
        openReportScreenWithType("LOST");
    }

    @FXML
    private void handleReportFound() {
        openReportScreenWithType("FOUND");
    }

    @FXML
    private void handleSearchItems() {
        SceneManager.switchScene("/view/search.fxml", "Search Items");
    }

    @FXML
    private void handleAdminPanel() {
        SceneManager.switchScene("/view/admin.fxml", "Admin Panel");
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        SceneManager.switchScene("/view/login.fxml", "Campus Lost and Found System");
    }

    private void loadDashboard() {
        try {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                SceneManager.switchScene("/view/login.fxml", "Campus Lost and Found System");
                return;
            }

            welcomeLabel.setText("Welcome, " + currentUser.getName());
            boolean admin = currentUser.isAdmin();
            adminButton.setVisible(admin);
            adminButton.setManaged(admin);
            lostCountLabel.setText(String.valueOf(itemService.countOpenLostItems()));
            foundCountLabel.setText(String.valueOf(itemService.countOpenFoundItems()));
            myReportsLabel.setText(String.valueOf(itemService.countUserReports(currentUser.getUserId())));
            myClaimsLabel.setText(String.valueOf(itemService.countUserClaims(currentUser.getUserId())));
        } catch (Exception exception) {
            AlertUtil.showError("Dashboard Error", exception.getMessage());
        }
    }

    private void openReportScreenWithType(String type) {
        SceneManager.switchScene("/view/report.fxml", "Report Item",
                controller -> ((ReportController) controller).setDefaultType(type));
    }
}
