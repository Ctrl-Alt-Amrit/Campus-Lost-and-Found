package com.lostfound.controller;

import com.lostfound.service.ItemService;
import com.lostfound.util.AlertUtil;
import com.lostfound.util.ImageUtil;
import com.lostfound.util.SceneManager;
import com.lostfound.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class ReportController {
    private final ItemService itemService = new ItemService();
    private File selectedImageFile;

    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private TextField titleField;
    @FXML
    private TextField locationField;
    @FXML
    private DatePicker itemDatePicker;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label selectedImageLabel;
    @FXML
    private Label titleCountLabel;
    @FXML
    private Label locationCountLabel;
    @FXML
    private Label descriptionCountLabel;
    @FXML
    private Label formStatusLabel;
    @FXML
    private Button submitReportButton;
    @FXML
    private ImageView previewImageView;

    @FXML
    public void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList("LOST", "FOUND"));
        typeComboBox.getSelectionModel().selectFirst();
        itemDatePicker.setValue(LocalDate.now());
        titleField.setTextFormatter(createTextLimitFormatter(150));
        locationField.setTextFormatter(createTextLimitFormatter(150));
        descriptionArea.setTextFormatter(createTextLimitFormatter(500));
        titleField.textProperty().addListener((observable, oldValue, newValue) -> updateFormState());
        locationField.textProperty().addListener((observable, oldValue, newValue) -> updateFormState());
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> updateFormState());
        itemDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updateFormState());
        selectedImageLabel.setText("No image selected");
        updateFormState();
    }

    public void setDefaultType(String type) {
        if (type != null && !type.isBlank()) {
            typeComboBox.setValue(type.toUpperCase());
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Item Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(SceneManager.getPrimaryStage());
        if (file != null) {
            selectedImageFile = file;
            selectedImageLabel.setText(file.getName());
            previewImageView.setImage(new Image(file.toURI().toString()));
            updateFormState();
        }
    }

    @FXML
    private void handleSubmitReport() {
        String imagePath = null;
        try {
            imagePath = selectedImageFile == null ? null : ImageUtil.copyImageToLocalFolder(selectedImageFile);
            itemService.reportItem(
                    SessionManager.getCurrentUser(),
                    typeComboBox.getValue(),
                    titleField.getText(),
                    descriptionArea.getText(),
                    locationField.getText(),
                    itemDatePicker.getValue(),
                    imagePath
            );
            AlertUtil.showInfo("Report Saved", "Your item report has been submitted successfully.");
            SceneManager.switchScene("/view/dashboard.fxml", "Dashboard");
        } catch (Exception exception) {
            rollbackImageCopy(imagePath);
            AlertUtil.showError("Unable to Save Report", exception.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/view/dashboard.fxml", "Dashboard");
    }

    private void rollbackImageCopy(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(Path.of(imagePath));
        } catch (Exception ignored) {
            // Best-effort cleanup for copied images when report submission fails.
        }
    }

    private TextFormatter<String> createTextLimitFormatter(int maxLength) {
        return new TextFormatter<>(change ->
                change.getControlNewText().length() <= maxLength ? change : null);
    }

    private void updateFormState() {
        int titleLength = getLength(titleField.getText());
        int locationLength = getLength(locationField.getText());
        int descriptionLength = getLength(descriptionArea.getText());
        boolean complete = titleLength > 0
                && locationLength > 0
                && descriptionLength > 0
                && itemDatePicker.getValue() != null;
        boolean futureDate = itemDatePicker.getValue() != null && itemDatePicker.getValue().isAfter(LocalDate.now());

        titleCountLabel.setText(titleLength + " / 150");
        locationCountLabel.setText(locationLength + " / 150");
        descriptionCountLabel.setText(descriptionLength + " / 500");
        submitReportButton.setDisable(!complete || futureDate);

        if (futureDate) {
            formStatusLabel.setText("Item date cannot be in the future.");
        } else if (complete) {
            formStatusLabel.setText(selectedImageFile == null
                    ? "Ready to submit. Add a photo if it helps identify the item."
                    : "Ready to submit with image: " + selectedImageFile.getName());
        } else {
            formStatusLabel.setText("Complete the required fields to submit the report.");
        }
    }

    private int getLength(String value) {
        return value == null ? 0 : value.trim().length();
    }
}
