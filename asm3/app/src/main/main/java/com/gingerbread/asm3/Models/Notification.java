package com.gingerbread.asm3.Models;

public class Notification {
    private String title;
    private String description;
    private boolean isRead;

    public Notification(String title, String description, boolean isRead) {
        this.title = title;
        this.description = description;
        this.isRead = isRead;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRead() {
        return isRead;
    }
}
