package com.gingerbread.asm3.Models;

public class Memory {
    private String memoryId;
    private String memoryName;
    private String date;
    private String note;
    private String imageUrl;
    private String userId;
    private String relationshipId;

    public Memory() {
    }

    public Memory(String memoryId, String memoryName, String date, String note, String imageUrl, String userId, String relationshipId) {
        this.memoryId = memoryId;
        this.memoryName = memoryName;
        this.date = date;
        this.note = note;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.relationshipId = relationshipId;
    }

    public String getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
    }

    public String getMemoryName() {
        return memoryName;
    }

    public void setMemoryName(String memoryName) {
        this.memoryName = memoryName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {
        this.relationshipId = relationshipId;
    }
}
