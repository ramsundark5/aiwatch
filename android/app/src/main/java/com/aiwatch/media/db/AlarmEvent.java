package com.aiwatch.media.db;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class AlarmEvent implements Serializable {

    @Id
    private long id;
    private String uuid;
    private String cameraName;
    private long cameraId;
    private String videoPath;
    private String cloudVideoPath;
    private String cloudImagePath;
    private String thumbnailPath;
    private Date date;
    private String message;
    private float detectionConfidence;
    private String userId; //this is the firebaseUserId
    private String deviceId; //deviceId sending notification

    public AlarmEvent(){
        super();
    }

    public AlarmEvent(long cameraId, String cameraName, Date date, String message, String videoPath, String thumbnailPath, String uuid) {
        this.cameraName = cameraName;
        this.videoPath = videoPath;
        this.cameraId = cameraId;
        this.date = date;
        this.message = message;
        this.thumbnailPath = thumbnailPath;
        this.uuid = uuid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public long getCameraId() {
        return cameraId;
    }

    public void setCameraId(long cameraId) {
        this.cameraId = cameraId;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getDetectionConfidence() {
        return detectionConfidence;
    }

    public void setDetectionConfidence(float detectionConfidence) {
        this.detectionConfidence = detectionConfidence;
    }

    public String getCloudVideoPath() {
        return cloudVideoPath;
    }

    public void setCloudVideoPath(String cloudVideoPath) {
        this.cloudVideoPath = cloudVideoPath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCloudImagePath() {
        return cloudImagePath;
    }

    public void setCloudImagePath(String cloudImagePath) {
        this.cloudImagePath = cloudImagePath;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}