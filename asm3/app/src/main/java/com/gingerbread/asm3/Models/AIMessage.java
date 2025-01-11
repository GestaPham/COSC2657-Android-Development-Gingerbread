package com.gingerbread.asm3.Models;

public class AIMessage {
    private String message;
    private String conversationId;
    private String receiverId;
    private String senderId;
    private long timestamp;

    public AIMessage() {}

    public AIMessage(String message, String conversationId, String receiverId, String senderId, long timestamp) {
        this.message = message;
        this.conversationId = conversationId;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
