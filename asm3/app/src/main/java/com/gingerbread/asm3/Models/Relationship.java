package com.gingerbread.asm3.Models;

public class Relationship {
    private String relationshipId;
    private String shareToken;
    private String startDate;
    private int daysTogether;
    private String relationshipStatus;

    public Relationship() {
    }

    public Relationship(String relationshipId, String shareToken, String startDate, int daysTogether, String relationshipStatus) {
        this.relationshipId = relationshipId;
        this.shareToken = shareToken;
        this.startDate = startDate;
        this.daysTogether = daysTogether;
        this.relationshipStatus = relationshipStatus;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {
        this.relationshipId = relationshipId;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public int getDaysTogether() {
        return daysTogether;
    }

    public void setDaysTogether(int daysTogether) {
        this.daysTogether = daysTogether;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }
}
