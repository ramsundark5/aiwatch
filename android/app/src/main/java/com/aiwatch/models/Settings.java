package com.aiwatch.models;

import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Settings {

    @Id
    private long id;
    private boolean isGoogleAccountConnected;
    private boolean isNotificationEnabled;
    private boolean isNoAdsPurchased;
    private String smartthingsAccessToken;
    private Date smartthingsRefreshToken;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isGoogleAccountConnected() {
        return isGoogleAccountConnected;
    }

    public void setGoogleAccountConnected(boolean googleAccountConnected) {
        isGoogleAccountConnected = googleAccountConnected;
    }

    public boolean isNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        isNotificationEnabled = notificationEnabled;
    }

    public boolean isNoAdsPurchased() {
        return isNoAdsPurchased;
    }

    public void setNoAdsPurchased(boolean noAdsPurchased) {
        isNoAdsPurchased = noAdsPurchased;
    }

    public String getSmartthingsAccessToken() {
        return smartthingsAccessToken;
    }

    public void setSmartthingsAccessToken(String smartthingsAccessToken) {
        this.smartthingsAccessToken = smartthingsAccessToken;
    }

    public Date getSmartthingsRefreshToken() {
        return smartthingsRefreshToken;
    }

    public void setSmartthingsRefreshToken(Date smartthingsRefreshToken) {
        this.smartthingsRefreshToken = smartthingsRefreshToken;
    }
}
