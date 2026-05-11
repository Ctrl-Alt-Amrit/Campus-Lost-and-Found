package com.lostfound.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminItemView {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final Integer itemId;
    private final String type;
    private final String title;
    private final String location;
    private final LocalDate itemDate;
    private final String status;
    private final String reporterName;
    private final String reporterEmail;
    private final Integer claimCount;

    public AdminItemView(Integer itemId, String type, String title, String location, LocalDate itemDate,
                         String status, String reporterName, String reporterEmail, Integer claimCount) {
        this.itemId = itemId;
        this.type = type;
        this.title = title;
        this.location = location;
        this.itemDate = itemDate;
        this.status = status;
        this.reporterName = reporterName;
        this.reporterEmail = reporterEmail;
        this.claimCount = claimCount;
    }

    public Integer getItemId() {
        return itemId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getItemDate() {
        return itemDate;
    }

    public String getFormattedDate() {
        return itemDate == null ? "Date not available" : itemDate.format(DATE_FORMATTER);
    }

    public String getStatus() {
        return status;
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public String getReporterSummary() {
        if (reporterName == null || reporterName.isBlank()) {
            return reporterEmail;
        }
        return reporterName + " <" + reporterEmail + ">";
    }

    public Integer getClaimCount() {
        return claimCount;
    }
}
