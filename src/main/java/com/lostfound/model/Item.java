package com.lostfound.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Item {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private Integer itemId;
    private Integer userId;
    private String type;
    private String title;
    private String description;
    private String location;
    private LocalDate itemDate;
    private String imagePath;
    private String status;

    public Item() {
    }

    public Item(Integer itemId, Integer userId, String type, String title, String description,
                String location, LocalDate itemDate, String imagePath, String status) {
        this.itemId = itemId;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.location = location;
        this.itemDate = itemDate;
        this.imagePath = imagePath;
        this.status = status;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getItemDate() {
        return itemDate;
    }

    public void setItemDate(LocalDate itemDate) {
        this.itemDate = itemDate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFoundItem() {
        return "FOUND".equalsIgnoreCase(type);
    }

    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(status);
    }

    public boolean isResolved() {
        return "RESOLVED".equalsIgnoreCase(status);
    }

    public boolean hasImage() {
        return imagePath != null && !imagePath.isBlank();
    }

    public String getFormattedDate() {
        return itemDate == null ? "Date not available" : itemDate.format(DATE_FORMATTER);
    }

    public String getDescriptionPreview(int maxLength) {
        if (description == null || description.isBlank()) {
            return "No description provided.";
        }

        String normalized = description.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= maxLength) {
            return normalized;
        }

        return normalized.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    @Override
    public String toString() {
        return title;
    }
}
