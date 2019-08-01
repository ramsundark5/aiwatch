package com.aiwatch.models;

import java.util.Date;
import java.util.Map;

public class FirebaseUserData {

    private String id;
    private String email;
    private Map<String, String> deviceTokens;
    private Date lastUpdated;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, String> getDeviceTokens() {
        return deviceTokens;
    }

    public void setDeviceTokens(Map<String, String> deviceTokens) {
        this.deviceTokens = deviceTokens;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
