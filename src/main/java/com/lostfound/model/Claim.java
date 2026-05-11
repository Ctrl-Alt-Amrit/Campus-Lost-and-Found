package com.lostfound.model;

public class Claim {
    private Integer claimId;
    private Integer itemId;
    private Integer claimantId;
    private String status;

    public Claim() {
    }

    public Claim(Integer claimId, Integer itemId, Integer claimantId, String status) {
        this.claimId = claimId;
        this.itemId = itemId;
        this.claimantId = claimantId;
        this.status = status;
    }

    public Integer getClaimId() {
        return claimId;
    }

    public void setClaimId(Integer claimId) {
        this.claimId = claimId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(Integer claimantId) {
        this.claimantId = claimantId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
