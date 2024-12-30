package com.gingerbread.asm3.Models;

public class User {
    private String userId;
    private String name;
    private String email;
    private String profilePictureUrl;
    private boolean isPremium;
    private String fcmToken;            // For push notifications
    private String shareToken;        // For linking with a partner

    public User() {
    }

    public User(String userId, String name, String email, String profilePictureUrl, boolean isPremium, String fcmToken, String shareToken) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
        this.isPremium = isPremium;
        this.fcmToken = fcmToken;
        this.shareToken = shareToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String notificationToken) {
        this.fcmToken = notificationToken;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }
}
