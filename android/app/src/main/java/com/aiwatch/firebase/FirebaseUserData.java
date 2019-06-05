package com.aiwatch.firebase;

import com.aiwatch.media.db.CameraConfig;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FirebaseUserData {

    private String id;
    private String email;
    private List<CameraConfig> cameraConfigList;
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

    public List<CameraConfig> getCameraConfigList() {
        return cameraConfigList;
    }

    public void setCameraConfigList(List<CameraConfig> cameraConfigList) {
        this.cameraConfigList = cameraConfigList;
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
