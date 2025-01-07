package com.gingerbread.asm3.Models;

public class Milestone {
    private String milestoneId;
    private String shareToken;
    private String description;
    private String dateAchieved;

    public Milestone() {
    }

    public Milestone(String milestoneId, String shareToken, String description, String dateAchieved) {
        this.milestoneId = milestoneId;
        this.shareToken = shareToken;
        this.description = description;
        this.dateAchieved = dateAchieved;
    }

    public String getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(String milestoneId) {
        this.milestoneId = milestoneId;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateAchieved() {
        return dateAchieved;
    }

    public void setDateAchieved(String dateAchieved) {
        this.dateAchieved = dateAchieved;
    }
}
