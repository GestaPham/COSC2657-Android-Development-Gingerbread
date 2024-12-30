package com.gingerbread.asm3.Models;

public class Milestone {
    private String milestoneId;
    private String userId;
    private String description;
    private String dateAchieved;

    public Milestone() {
    }

    public Milestone(String milestoneId, String userId, String description, String dateAchieved) {
        this.milestoneId = milestoneId;
        this.userId = userId;
        this.description = description;
        this.dateAchieved = dateAchieved;
    }

    public String getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(String milestoneId) {
        this.milestoneId = milestoneId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
