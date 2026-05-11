package com.lostfound.controller;

import com.lostfound.model.Item;
import com.lostfound.model.User;
import com.lostfound.service.ItemService;
import com.lostfound.util.AlertUtil;
import com.lostfound.util.ImageUtil;
import com.lostfound.util.SceneManager;
import com.lostfound.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DetailController {
    private final ItemService itemService = new ItemService();
    private Item item;

    @FXML
    private Label titleLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label imageStatusLabel;
    @FXML
    private Label claimHintLabel;
    @FXML
    private ImageView itemImageView;
    @FXML
    private Button claimButton;

    public void setItem(Item item) {
        this.item = item;
        displayItem();
    }

    @FXML
    private void handleClaimItem() {
        try {
            itemService.claimItem(item, SessionManager.getCurrentUser());
            item = itemService.getItemById(item.getItemId());
            displayItem();
            AlertUtil.showInfo("Claim Submitted", "Your claim has been submitted successfully.");
        } catch (Exception exception) {
            AlertUtil.showError("Claim Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/view/search.fxml", "Search Items");
    }

    @FXML
    private void handleDashboard() {
        SceneManager.switchScene("/view/dashboard.fxml", "Dashboard");
    }

    private void displayItem() {
        if (item == null || titleLabel == null) {
            return;
        }

        titleLabel.setText(item.getTitle());
        typeLabel.setText(item.getType());
        locationLabel.setText(item.getLocation());
        dateLabel.setText(item.getFormattedDate());
        statusLabel.setText(item.getStatus());
        descriptionLabel.setText(item.getDescription());
        updateBadgeStyles();

        Image image = ImageUtil.loadImage(item.getImagePath());
        if (image != null) {
            itemImageView.setImage(image);
            imageStatusLabel.setText("Uploaded image preview available");
        } else {
            itemImageView.setImage(null);
            imageStatusLabel.setText("No image available");
        }

        User currentUser = SessionManager.getCurrentUser();
        boolean canClaim = currentUser != null
                && !item.getUserId().equals(currentUser.getUserId())
                && item.isFoundItem()
                && item.isOpen();

        claimButton.setVisible(canClaim);
        claimButton.setManaged(canClaim);
        claimHintLabel.setText(getClaimHint(currentUser, canClaim));
    }

    private void updateBadgeStyles() {
        typeLabel.getStyleClass().setAll("pill", item.isFoundItem() ? "pill-found" : "pill-lost");
        String statusClass = item.isOpen() ? "pill-open" : item.isResolved() ? "pill-resolved" : "pill-claimed";
        statusLabel.getStyleClass().setAll("pill", statusClass);
    }

    private String getClaimHint(User currentUser, boolean canClaim) {
        if (canClaim) {
            return "This found item is open. Submit a claim if it belongs to you.";
        }
        if (currentUser == null) {
            return "Sign in to submit a claim.";
        }
        if (item.getUserId().equals(currentUser.getUserId())) {
            return "You reported this item, so it cannot be claimed from your own account.";
        }
        if (!item.isFoundItem()) {
            return "Lost reports are for discovery only. Claims are available on found items.";
        }
        if (!item.isOpen()) {
            return "This item is no longer open for claims.";
        }
        return "This record cannot be claimed right now.";
    }
}
