package com.aiwatch.models;

import com.google.common.base.Strings;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;
import io.objectbox.relation.ToOne;

@Entity
public class CameraConfig implements Serializable {
    @Id
    private long id;
    @Unique
    private String uuid;
    private String name;
    private String brand;
    private String model;
    private String videoUrl;

    @Exclude
    private String videoUrlWithAuth;
    private String username;

    @Exclude
    private String password;

    private int videoCodec;
    private int recordingDuration;
    private int waitPeriodAfterDetection;
    private boolean notifyPersonDetect;
    private boolean notifyAnimalDetect;
    private boolean notifyVehicleDetect;
    private boolean recordPersonDetect;
    private boolean recordAnimalDetect;
    private boolean recordVehicleDetect;
    private boolean testModeEnabled;
    private boolean cvrEnabled;
    private boolean disconnected;
    private Date lastModified;

    private float topLeftX = 0;
    private float topLeftY = 0;
    private float topRightX = 300;
    private float topRightY = 0;
    private float bottomLeftX =0 ;
    private float bottomLeftY = 300;
    private float bottomRightX = 300;
    private float bottomRightY = 300;

    private boolean monitoringEnabled;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(int videoCodec) {
        this.videoCodec = videoCodec;
    }

    public boolean isNotifyPersonDetect() {
        return notifyPersonDetect;
    }

    public void setNotifyPersonDetect(boolean notifyPersonDetect) {
        this.notifyPersonDetect = notifyPersonDetect;
    }

    public boolean isNotifyAnimalDetect() {
        return notifyAnimalDetect;
    }

    public void setNotifyAnimalDetect(boolean notifyAnimalDetect) {
        this.notifyAnimalDetect = notifyAnimalDetect;
    }

    public boolean isNotifyVehicleDetect() {
        return notifyVehicleDetect;
    }

    public void setNotifyVehicleDetect(boolean notifyVehicleDetect) {
        this.notifyVehicleDetect = notifyVehicleDetect;
    }

    public boolean isRecordPersonDetect() {
        return recordPersonDetect;
    }

    public void setRecordPersonDetect(boolean recordPersonDetect) {
        this.recordPersonDetect = recordPersonDetect;
    }

    public boolean isRecordAnimalDetect() {
        return recordAnimalDetect;
    }

    public void setRecordAnimalDetect(boolean recordAnimalDetect) {
        this.recordAnimalDetect = recordAnimalDetect;
    }

    public boolean isRecordVehicleDetect() {
        return recordVehicleDetect;
    }

    public void setRecordVehicleDetect(boolean recordVehicleDetect) {
        this.recordVehicleDetect = recordVehicleDetect;
    }

    public int getRecordingDuration() {
        return recordingDuration;
    }

    public void setRecordingDuration(int recordingDuration) {
        this.recordingDuration = recordingDuration;
    }

    public int getWaitPeriodAfterDetection() {
        return waitPeriodAfterDetection;
    }

    public void setWaitPeriodAfterDetection(int waitPeriodAfterDetection) {
        this.waitPeriodAfterDetection = waitPeriodAfterDetection;
    }

    public boolean isTestModeEnabled() {
        return testModeEnabled;
    }

    public void setTestModeEnabled(boolean testModeEnabled) {
        this.testModeEnabled = testModeEnabled;
    }

    public boolean isCvrEnabled() {
        return cvrEnabled;
    }

    public void setCvrEnabled(boolean cvrEnabled) {
        this.cvrEnabled = cvrEnabled;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public float getTopLeftX() {
        return topLeftX;
    }

    public void setTopLeftX(float topLeftX) {
        this.topLeftX = topLeftX;
    }

    public float getTopLeftY() {
        return topLeftY;
    }

    public void setTopLeftY(float topLeftY) {
        this.topLeftY = topLeftY;
    }

    public float getTopRightX() {
        return topRightX;
    }

    public void setTopRightX(float topRightX) {
        this.topRightX = topRightX;
    }

    public float getTopRightY() {
        return topRightY;
    }

    public void setTopRightY(float topRightY) {
        this.topRightY = topRightY;
    }

    public float getBottomLeftX() {
        return bottomLeftX;
    }

    public void setBottomLeftX(float bottomLeftX) {
        this.bottomLeftX = bottomLeftX;
    }

    public float getBottomLeftY() {
        return bottomLeftY;
    }

    public void setBottomLeftY(float bottomLeftY) {
        this.bottomLeftY = bottomLeftY;
    }

    public float getBottomRightX() {
        return bottomRightX;
    }

    public void setBottomRightX(float bottomRightX) {
        this.bottomRightX = bottomRightX;
    }

    public float getBottomRightY() {
        return bottomRightY;
    }

    public void setBottomRightY(float bottomRightY) {
        this.bottomRightY = bottomRightY;
    }

    @Exclude
    public boolean isMonitoringEnabled(){
        boolean monitoringEnabled = notifyPersonDetect || recordPersonDetect
                || notifyAnimalDetect || recordAnimalDetect
                || notifyVehicleDetect || recordVehicleDetect
                || testModeEnabled;
        return monitoringEnabled;
    }

    public void setVideoUrlWithAuth(String videoUrlWithAuth) {
        this.videoUrlWithAuth = videoUrlWithAuth;
    }

    @Exclude
    public String getVideoUrlWithAuth(){
        if(!Strings.isNullOrEmpty(this.username) && !Strings.isNullOrEmpty(this.password)){
            int insertIndex = this.videoUrl.indexOf("://") + 3; //+3 is to add credentials at end of string match
            String authStr = this.username + ":" + this.password + "@";
            String urlWithAuth = new StringBuilder(this.videoUrl).insert(insertIndex, authStr).toString();
            return urlWithAuth;
        }
        return this.videoUrl;
    }
}
