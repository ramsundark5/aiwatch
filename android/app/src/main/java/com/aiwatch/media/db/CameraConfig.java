package com.aiwatch.media.db;

import java.io.Serializable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class CameraConfig implements Serializable {
    @Id
    private long id;
    private String name;
    private String brand;
    private String model;
    private String videoUrl;
    private String username;
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

    public boolean isMonitoringEnabled(){
        boolean monitoringEnabled = notifyPersonDetect || recordPersonDetect
                || notifyAnimalDetect || recordAnimalDetect
                || notifyVehicleDetect || recordVehicleDetect;
        return monitoringEnabled;
    }
}
