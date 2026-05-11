package com.lostfound.controller;

import com.lostfound.model.AdminClaimView;
import com.lostfound.model.AdminItemView;
import com.lostfound.model.User;
import com.lostfound.service.AdminService;
import com.lostfound.util.AlertUtil;
import com.lostfound.util.SceneManager;
import com.lostfound.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.function.Function;

public class AdminController {
    private final AdminService adminService = new AdminService();

    @FXML
    private Label adminNameLabel;
    @FXML
    private Label reportCountLabel;
    @FXML
    private Label pendingClaimsLabel;
    @FXML
    private Label usersCountLabel;
    @FXML
    private Label resolvedCountLabel;
    @FXML
    private Label reportSelectionLabel;
    @FXML
    private Label claimSelectionLabel;
    @FXML
    private Label userSelectionLabel;
    @FXML
    private Button markResolvedButton;
    @FXML
    private Button removeReportButton;
    @FXML
    private Button approveClaimButton;
    @FXML
    private Button rejectClaimButton;
    @FXML
    private Button makeAdminButton;
    @FXML
    private Button makeUserButton;
    @FXML
    private Button activateUserButton;
    @FXML
    private Button disableUserButton;

    @FXML
    private TableView<AdminItemView> reportsTable;
    @FXML
    private TableColumn<AdminItemView, Integer> reportIdColumn;
    @FXML
    private TableColumn<AdminItemView, String> reportTypeColumn;
    @FXML
    private TableColumn<AdminItemView, String> reportTitleColumn;
    @FXML
    private TableColumn<AdminItemView, String> reportReporterColumn;
    @FXML
    private TableColumn<AdminItemView, String> reportLocationColumn;
    @FXML
    private TableColumn<AdminItemView, String> reportDateColumn;
    @FXML
    private TableColumn<AdminItemView, String> reportStatusColumn;
    @FXML
    private TableColumn<AdminItemView, Integer> reportClaimsColumn;

    @FXML
    private TableView<AdminClaimView> claimsTable;
    @FXML
    private TableColumn<AdminClaimView, Integer> claimIdColumn;
    @FXML
    private TableColumn<AdminClaimView, String> claimItemColumn;
    @FXML
    private TableColumn<AdminClaimView, String> claimTypeColumn;
    @FXML
    private TableColumn<AdminClaimView, String> claimClaimantColumn;
    @FXML
    private TableColumn<AdminClaimView, String> claimReporterColumn;
    @FXML
    private TableColumn<AdminClaimView, String> claimItemStatusColumn;
    @FXML
    private TableColumn<AdminClaimView, String> claimStatusColumn;

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> userIdColumn;
    @FXML
    private TableColumn<User, String> userNameColumn;
    @FXML
    private TableColumn<User, String> userEmailColumn;
    @FXML
    private TableColumn<User, String> userRoleColumn;
    @FXML
    private TableColumn<User, String> userStatusColumn;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            SceneManager.switchScene("/view/login.fxml", "Campus Lost and Found System");
            return;
        }
        if (!currentUser.isAdmin()) {
            SceneManager.switchScene("/view/dashboard.fxml", "Dashboard");
            return;
        }

        adminNameLabel.setText("Signed in as " + currentUser.getName());
        configureReportsTable();
        configureClaimsTable();
        configureUsersTable();
        refreshAll();
    }

    @FXML
    private void handleRefresh() {
        refreshAll();
    }

    @FXML
    private void handleMarkResolved() {
        AdminItemView report = reportsTable.getSelectionModel().getSelectedItem();
        if (report == null) {
            AlertUtil.showWarning("No Report Selected", "Choose a report before marking it resolved.");
            return;
        }

        try {
            adminService.markReportResolved(getCurrentAdmin(), report.getItemId());
            refreshAll();
            AlertUtil.showInfo("Report Updated", "The selected item has been marked as resolved.");
        } catch (Exception exception) {
            AlertUtil.showError("Update Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleRemoveReport() {
        AdminItemView report = reportsTable.getSelectionModel().getSelectedItem();
        if (report == null) {
            AlertUtil.showWarning("No Report Selected", "Choose a report before removing it.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Remove Report",
                "Remove this report and all of its claims? This action cannot be undone."
        );
        if (!confirmed) {
            return;
        }

        try {
            adminService.removeReport(getCurrentAdmin(), report.getItemId());
            refreshAll();
            AlertUtil.showInfo("Report Removed", "The selected report has been removed.");
        } catch (Exception exception) {
            AlertUtil.showError("Remove Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleApproveClaim() {
        AdminClaimView claim = claimsTable.getSelectionModel().getSelectedItem();
        if (claim == null) {
            AlertUtil.showWarning("No Claim Selected", "Choose a claim before approving it.");
            return;
        }

        try {
            adminService.approveClaim(getCurrentAdmin(), claim.getClaimId());
            refreshAll();
            AlertUtil.showInfo("Claim Approved", "The claim was approved and the item was marked resolved.");
        } catch (Exception exception) {
            AlertUtil.showError("Approval Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleRejectClaim() {
        AdminClaimView claim = claimsTable.getSelectionModel().getSelectedItem();
        if (claim == null) {
            AlertUtil.showWarning("No Claim Selected", "Choose a claim before rejecting it.");
            return;
        }

        try {
            adminService.rejectClaim(getCurrentAdmin(), claim.getClaimId());
            refreshAll();
            AlertUtil.showInfo("Claim Rejected", "The claim was rejected.");
        } catch (Exception exception) {
            AlertUtil.showError("Rejection Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleMakeAdmin() {
        updateSelectedUserRole("ADMIN");
    }

    @FXML
    private void handleMakeUser() {
        updateSelectedUserRole("USER");
    }

    @FXML
    private void handleActivateUser() {
        updateSelectedUserActive(true);
    }

    @FXML
    private void handleDisableUser() {
        updateSelectedUserActive(false);
    }

    @FXML
    private void handleUserDashboard() {
        SceneManager.switchScene("/view/dashboard.fxml", "Dashboard");
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        SceneManager.switchScene("/view/login.fxml", "Campus Lost and Found System");
    }

    private void configureReportsTable() {
        reportsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reportIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        reportTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        reportTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        reportReporterColumn.setCellValueFactory(new PropertyValueFactory<>("reporterSummary"));
        reportLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        reportDateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        reportStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        reportClaimsColumn.setCellValueFactory(new PropertyValueFactory<>("claimCount"));
        configurePillColumn(reportTypeColumn, this::getTypePillClass);
        configureTextColumn(reportTitleColumn);
        configureTextColumn(reportReporterColumn);
        configureTextColumn(reportLocationColumn);
        configurePillColumn(reportStatusColumn, this::getStatusPillClass);
        reportsTable.setPlaceholder(new Label("No item reports found."));
        reportsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldReport, newReport) -> updateReportActions(newReport));
        updateReportActions(null);
    }

    private void configureClaimsTable() {
        claimsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        claimIdColumn.setCellValueFactory(new PropertyValueFactory<>("claimId"));
        claimItemColumn.setCellValueFactory(new PropertyValueFactory<>("itemTitle"));
        claimTypeColumn.setCellValueFactory(new PropertyValueFactory<>("itemType"));
        claimClaimantColumn.setCellValueFactory(new PropertyValueFactory<>("claimantSummary"));
        claimReporterColumn.setCellValueFactory(new PropertyValueFactory<>("reporterName"));
        claimItemStatusColumn.setCellValueFactory(new PropertyValueFactory<>("itemStatus"));
        claimStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        configureTextColumn(claimItemColumn);
        configurePillColumn(claimTypeColumn, this::getTypePillClass);
        configureTextColumn(claimClaimantColumn);
        configureTextColumn(claimReporterColumn);
        configurePillColumn(claimItemStatusColumn, this::getStatusPillClass);
        configurePillColumn(claimStatusColumn, this::getStatusPillClass);
        claimsTable.setPlaceholder(new Label("No claims have been submitted."));
        claimsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldClaim, newClaim) -> updateClaimActions(newClaim));
        updateClaimActions(null);
    }

    private void configureUsersTable() {
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userStatusColumn.setCellValueFactory(new PropertyValueFactory<>("activeLabel"));
        configureTextColumn(userNameColumn);
        configureTextColumn(userEmailColumn);
        configurePillColumn(userRoleColumn, this::getRolePillClass);
        configurePillColumn(userStatusColumn, this::getUserStatusPillClass);
        usersTable.setPlaceholder(new Label("No users found."));
        usersTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldUser, newUser) -> updateUserActions(newUser));
        updateUserActions(null);
    }

    private void refreshAll() {
        try {
            User admin = getCurrentAdmin();
            reportsTable.setItems(FXCollections.observableArrayList(adminService.getReports(admin)));
            claimsTable.setItems(FXCollections.observableArrayList(adminService.getClaims(admin)));
            usersTable.setItems(FXCollections.observableArrayList(adminService.getUsers(admin)));
            reportCountLabel.setText(String.valueOf(adminService.countReports(admin)));
            pendingClaimsLabel.setText(String.valueOf(adminService.countPendingClaims(admin)));
            usersCountLabel.setText(String.valueOf(adminService.countUsers(admin)));
            resolvedCountLabel.setText(String.valueOf(adminService.countResolvedReports(admin)));
            updateReportActions(reportsTable.getSelectionModel().getSelectedItem());
            updateClaimActions(claimsTable.getSelectionModel().getSelectedItem());
            updateUserActions(usersTable.getSelectionModel().getSelectedItem());
        } catch (Exception exception) {
            AlertUtil.showError("Admin Panel Error", exception.getMessage());
        }
    }

    private void updateSelectedUserRole(String role) {
        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user == null) {
            AlertUtil.showWarning("No User Selected", "Choose a user before changing the role.");
            return;
        }

        try {
            adminService.setUserRole(getCurrentAdmin(), user.getUserId(), role);
            refreshAll();
        } catch (Exception exception) {
            AlertUtil.showError("Role Update Failed", exception.getMessage());
        }
    }

    private void updateSelectedUserActive(boolean active) {
        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user == null) {
            AlertUtil.showWarning("No User Selected", "Choose a user before changing account status.");
            return;
        }

        try {
            adminService.setUserActive(getCurrentAdmin(), user.getUserId(), active);
            refreshAll();
        } catch (Exception exception) {
            AlertUtil.showError("User Update Failed", exception.getMessage());
        }
    }

    private User getCurrentAdmin() {
        return SessionManager.getCurrentUser();
    }

    private void updateReportActions(AdminItemView report) {
        boolean hasSelection = report != null;
        boolean resolved = hasSelection && "RESOLVED".equalsIgnoreCase(report.getStatus());
        markResolvedButton.setDisable(!hasSelection || resolved);
        removeReportButton.setDisable(!hasSelection);
        reportSelectionLabel.setText(hasSelection
                ? "Selected report #" + report.getItemId() + ": " + report.getTitle()
                + " | " + report.getStatus() + " | " + report.getReporterSummary()
                : "Select a report to manage it.");
    }

    private void updateClaimActions(AdminClaimView claim) {
        boolean hasSelection = claim != null;
        boolean pending = hasSelection && "PENDING".equalsIgnoreCase(claim.getStatus());
        boolean rejected = hasSelection && "REJECTED".equalsIgnoreCase(claim.getStatus());
        approveClaimButton.setDisable(!pending);
        rejectClaimButton.setDisable(!hasSelection || rejected);
        claimSelectionLabel.setText(hasSelection
                ? "Selected claim #" + claim.getClaimId() + ": " + claim.getItemTitle()
                + " requested by " + claim.getClaimantSummary() + " | " + claim.getStatus()
                : "Select a pending claim to approve or reject it.");
    }

    private void updateUserActions(User user) {
        User currentAdmin = getCurrentAdmin();
        boolean hasSelection = user != null;
        boolean currentUser = hasSelection && currentAdmin != null && user.getUserId().equals(currentAdmin.getUserId());
        boolean admin = hasSelection && user.isAdmin();
        boolean active = hasSelection && user.isActive();
        makeAdminButton.setDisable(!hasSelection || admin);
        makeUserButton.setDisable(!hasSelection || !admin || currentUser);
        activateUserButton.setDisable(!hasSelection || active);
        disableUserButton.setDisable(!hasSelection || !active || currentUser);
        userSelectionLabel.setText(hasSelection
                ? "Selected user #" + user.getUserId() + ": " + user.getName()
                + " | " + user.getRole() + " | " + user.getActiveLabel()
                : "Select a user to manage role or account status.");
    }

    private <S> void configureTextColumn(TableColumn<S, String> column) {
        column.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isBlank()) {
                    setText(null);
                    setTooltip(null);
                    return;
                }

                setText(value);
                setTooltip(new Tooltip(value));
            }
        });
    }

    private <S> void configurePillColumn(TableColumn<S, String> column, Function<String, String> styleResolver) {
        column.setCellFactory(tableColumn -> new TableCell<>() {
            private final Label pill = new Label();

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                pill.setText(value.toUpperCase());
                pill.getStyleClass().setAll("pill", styleResolver.apply(value));
                setText(null);
                setGraphic(pill);
            }
        });
    }

    private String getTypePillClass(String type) {
        return "FOUND".equalsIgnoreCase(type) ? "pill-found" : "pill-lost";
    }

    private String getRolePillClass(String role) {
        return "ADMIN".equalsIgnoreCase(role) ? "pill-approved" : "pill-open";
    }

    private String getUserStatusPillClass(String status) {
        return "ACTIVE".equalsIgnoreCase(status) ? "pill-approved" : "pill-rejected";
    }

    private String getStatusPillClass(String status) {
        if ("OPEN".equalsIgnoreCase(status)) {
            return "pill-open";
        }
        if ("CLAIMED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status)) {
            return "pill-pending";
        }
        if ("RESOLVED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
            return "pill-approved";
        }
        if ("REJECTED".equalsIgnoreCase(status)) {
            return "pill-rejected";
        }
        return "pill-closed";
    }
}
