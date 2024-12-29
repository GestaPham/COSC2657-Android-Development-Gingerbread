package com.gingerbread.asm3.Models;

public class Relationship {
    private String relationshipId;
    private String userId1;
    private String userId2;
    private String startDate;
    private int daysTogether;
    private String relationshipStatus;

    public Relationship() {
    }

    public Relationship(String relationshipId, String userId1, String userId2, String startDate, int daysTogether, String relationshipStatus) {
        this.relationshipId = relationshipId;
        this.userId1 = userId1;
        this.userId2 = userId2;
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

    public String getUserId1() {
        return userId1;
    }

    public void setUserId1(String userId1) {
        this.userId1 = userId1;
    }

    public String getUserId2() {
        return userId2;
    }

    public void setUserId2(String userId2) {
        this.userId2 = userId2;
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

