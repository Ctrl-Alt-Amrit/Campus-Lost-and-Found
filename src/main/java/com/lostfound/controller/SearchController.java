package com.lostfound.controller;

import com.lostfound.model.Item;
import com.lostfound.service.ItemService;
import com.lostfound.util.AlertUtil;
import com.lostfound.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class SearchController {
    private final ItemService itemService = new ItemService();

    @FXML
    private TextField keywordField;
    @FXML
    private ListView<Item> resultsListView;
    @FXML
    private Label resultsLabel;
    @FXML
    private Label selectionLabel;
    @FXML
    private Button viewDetailsButton;

    @FXML
    public void initialize() {
        resultsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.getStyleClass().add("result-title");

                    Label typePill = buildPill(item.getType(), item.isFoundItem() ? "pill-found" : "pill-lost");
                    Label statusPill = buildPill(item.getStatus(), getStatusPillClass(item));

                    HBox badges = new HBox(8, typePill, statusPill);
                    badges.setAlignment(Pos.CENTER_LEFT);

                    Label metaLabel = new Label(item.getLocation() + " | " + item.getFormattedDate());
                    metaLabel.getStyleClass().add("result-meta");

                    Label descriptionLabel = new Label(item.getDescriptionPreview(110));
                    descriptionLabel.getStyleClass().add("result-description");
                    descriptionLabel.setWrapText(true);

                    VBox textContent = new VBox(6, badges, titleLabel, metaLabel, descriptionLabel);
                    HBox.setHgrow(textContent, Priority.ALWAYS);

                    HBox card = new HBox(textContent);
                    card.getStyleClass().add("result-card");

                    setText(null);
                    setGraphic(card);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        resultsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleViewDetails();
            }
        });
        resultsListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldItem, newItem) -> updateSelectionState(newItem));
        resultsListView.setPlaceholder(createPlaceholderLabel());
        keywordField.setOnAction(event -> handleSearch());
        updateSelectionState(null);

        loadItems("");
    }

    @FXML
    private void handleSearch() {
        loadItems(keywordField.getText());
    }

    @FXML
    private void handleViewDetails() {
        Item selectedItem = resultsListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            AlertUtil.showWarning("No Selection", "Please choose an item from the search results.");
            return;
        }

        SceneManager.switchScene("/view/detail.fxml", "Item Details",
                controller -> ((DetailController) controller).setItem(selectedItem));
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/view/dashboard.fxml", "Dashboard");
    }

    private void loadItems(String keyword) {
        try {
            List<Item> items = itemService.searchItems(keyword);
            resultsListView.setItems(FXCollections.observableArrayList(items));
            String normalizedKeyword = keyword == null ? "" : keyword.trim();
            resultsLabel.setText(normalizedKeyword.isEmpty()
                    ? items.size() + " recent item(s)"
                    : items.size() + " result(s) for \"" + normalizedKeyword + "\"");
            updateSelectionState(null);
        } catch (Exception exception) {
            AlertUtil.showError("Search Failed", exception.getMessage());
        }
    }

    private Label buildPill(String text, String variantClass) {
        Label label = new Label(text == null ? "UNKNOWN" : text.toUpperCase());
        label.getStyleClass().addAll("pill", variantClass);
        return label;
    }

    private String getStatusPillClass(Item item) {
        if (item.isOpen()) {
            return "pill-open";
        }
        if (item.isResolved()) {
            return "pill-resolved";
        }
        return "pill-claimed";
    }

    private Label createPlaceholderLabel() {
        Label placeholder = new Label("No matching items yet. Try a broader keyword or report a new item.");
        placeholder.getStyleClass().add("empty-state");
        placeholder.setWrapText(true);
        placeholder.setMaxWidth(320);
        return placeholder;
    }

    private void updateSelectionState(Item item) {
        viewDetailsButton.setDisable(item == null);
        selectionLabel.setText(item == null
                ? "Select a result to inspect the full record."
                : "Selected: " + item.getTitle() + " | " + item.getType() + " | " + item.getStatus());
    }
}
