package com.gingerbread.asm3.Models;

import java.util.Map;

public class Message {
    private String messageRoomId;
    private String senderId;
    private String messageContent;
    private String timeStamp;
    private Map<String,Boolean> readStatus;

    public Message() {
    }

    public Message(String messageRoomId, String senderId, String messageContent, String timeStamp) {
        this.messageRoomId = messageRoomId;
        this.senderId = senderId;
        this.messageContent = messageContent;
        this.timeStamp = timeStamp;
    }

    public String getMessageRoomId() {
        return messageRoomId;
    }

    public void setMessageRoomId(String messageRoomId) {
        this.messageRoomId = messageRoomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
    public Map<String, Boolean> getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(Map<String, Boolean> readStatus) {
        this.readStatus = readStatus;
    }
}
