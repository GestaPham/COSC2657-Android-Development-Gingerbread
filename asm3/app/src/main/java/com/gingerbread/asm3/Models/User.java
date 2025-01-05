package com.gingerbread.asm3.Models;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String name;
    private String email;
    private String profilePictureUrl;
    private int age;
    private String gender;
    private String nationality;
    private String religion;
    private String location;
    private boolean isPremium;
    private String fcmToken;            // For push notifications
    private String shareToken;          // For linking with a partner
    private String pendingPartner;      // check if there's a pending partner request

    public User() {
    }

    public User(String userId, String name, String email, String profilePictureUrl, int age, String gender,
                String nationality, String religion, String location, boolean isPremium, String fcmToken,
                String shareToken, String pendingPartner) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
        this.age = age;
        this.gender = gender;
        this.nationality = nationality;
        this.religion = religion;
        this.location = location;
        this.isPremium = isPremium;
        this.fcmToken = fcmToken;
        this.shareToken = shareToken;
        this.pendingPartner = pendingPartner;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public String getPendingPartner() {
        return pendingPartner;
    }

    public void setPendingPartner(String pendingPartner) {
        this.pendingPartner = pendingPartner;
    }
}
