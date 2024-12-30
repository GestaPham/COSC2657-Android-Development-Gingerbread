package com.gingerbread.asm3.Models;

public class MoodLog {
    private String logId;
    private String userId;
    private String date;
    private String mood;
    private String notes;

    public MoodLog() {
    }

    public MoodLog(String logId, String userId, String date, String mood, String notes) {
        this.logId = logId;
        this.userId = userId;
        this.date = date;
        this.mood = mood;
        this.notes = notes;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

