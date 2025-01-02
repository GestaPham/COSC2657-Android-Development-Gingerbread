package com.gingerbread.asm3.Models;

public class Memory {
    private String memoryId;
    private String memoryName;
    private String date;
    private String note;
    private String imageUrl;

    public Memory() {
    }

    public Memory(String memoryId, String memoryName, String date, String note, String imageUrl) {
        this.memoryId = memoryId;
        this.memoryName = memoryName;
        this.date = date;
        this.note = note;
        this.imageUrl = imageUrl;
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
}
