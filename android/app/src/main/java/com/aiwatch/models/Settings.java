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
    private boolean isAlexaConnected;
    private String smartthingsClientId;
    private String smartthingsClientSecret;
    private String smartthingsAccessToken;
    private String smartAppEndpoint;
    private Date smartthingsAccessTokenExpiry;
    private String alexaToken;

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

    public Date getSmartthingsAccessTokenExpiry() {
        return smartthingsAccessTokenExpiry;
    }

    public void setSmartthingsAccessTokenExpiry(Date smartthingsAccessTokenExpiry) {
        this.smartthingsAccessTokenExpiry = smartthingsAccessTokenExpiry;
    }

    public String getSmartAppEndpoint() {
        return smartAppEndpoint;
    }

    public void setSmartAppEndpoint(String smartAppEndpoint) {
        this.smartAppEndpoint = smartAppEndpoint;
    }

    public String getAlexaToken() {
        return alexaToken;
    }

    public void setAlexaToken(String alexaToken) {
        this.alexaToken = alexaToken;
    }

    public String getSmartthingsClientId() {
        return smartthingsClientId;
    }

    public void setSmartthingsClientId(String smartthingsClientId) {
        this.smartthingsClientId = smartthingsClientId;
    }

    public String getSmartthingsClientSecret() {
        return smartthingsClientSecret;
    }

    public void setSmartthingsClientSecret(String smartthingsClientSecret) {
        this.smartthingsClientSecret = smartthingsClientSecret;
    }

    public boolean isAlexaConnected() {
        return isAlexaConnected;
    }

    public void setAlexaConnected(boolean alexaConnected) {
        isAlexaConnected = alexaConnected;
    }
}
