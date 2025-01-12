package com.gingerbread.asm3.Models;

import com.google.firebase.Timestamp;

import java.util.List;

public class Notification {
    private String notificationId;
    private String title;
    private String description;
    private String relationshipId;
    private List<String> readBy;
    private boolean isRead;
    private String type;
    private Timestamp timestamp;

    public Notification() {
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
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

    public String getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {
        this.relationshipId = relationshipId;
    }

    public List<String> getReadBy() {
        return readBy;
    }

    public void setReadBy(List<String> readBy) {
        this.readBy = readBy;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
