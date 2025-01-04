package com.gingerbread.asm3.Models;

import java.util.List;

public class MessageRoom {
    private String messageRoomId;
    private List<String> userIds;
    private String lastMessageSenderId;
    private String lastMessageTimeStamp;

    public MessageRoom(String messageRoomId, List<String> userIds, String lastMessageSenderId, String lastMessageTimeStamp) {
        this.messageRoomId = messageRoomId;
        this.userIds = userIds;
        this.lastMessageSenderId = lastMessageSenderId;
        this.lastMessageTimeStamp = lastMessageTimeStamp;
    }

    public MessageRoom() {
    }

    public String getMessageRoomId() {
        return messageRoomId;
    }

    public void setMessageRoomId(String messageRoomId) {
        this.messageRoomId = messageRoomId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getLastMessageTimeStamp() {
        return lastMessageTimeStamp;
    }

    public void setLastMessageTimeStamp(String lastMessageTimeStamp) {
        this.lastMessageTimeStamp = lastMessageTimeStamp;
    }
}
