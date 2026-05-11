package com.lostfound.model;

public class AdminClaimView {
    private final Integer claimId;
    private final Integer itemId;
    private final String itemTitle;
    private final String itemType;
    private final String itemStatus;
    private final String location;
    private final Integer claimantId;
    private final String claimantName;
    private final String claimantEmail;
    private final String reporterName;
    private final String status;

    public AdminClaimView(Integer claimId, Integer itemId, String itemTitle, String itemType,
                          String itemStatus, String location, Integer claimantId, String claimantName,
                          String claimantEmail, String reporterName, String status) {
        this.claimId = claimId;
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.itemType = itemType;
        this.itemStatus = itemStatus;
        this.location = location;
        this.claimantId = claimantId;
        this.claimantName = claimantName;
        this.claimantEmail = claimantEmail;
        this.reporterName = reporterName;
        this.status = status;
    }

    public Integer getClaimId() {
        return claimId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemType() {
        return itemType;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public String getLocation() {
        return location;
    }

    public Integer getClaimantId() {
        return claimantId;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public String getClaimantEmail() {
        return claimantEmail;
    }

    public String getClaimantSummary() {
        if (claimantName == null || claimantName.isBlank()) {
            return claimantEmail;
        }
        return claimantName + " <" + claimantEmail + ">";
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getStatus() {
        return status;
    }
}
