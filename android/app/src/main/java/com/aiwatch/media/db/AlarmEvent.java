package com.aiwatch.media.db;

import java.io.Serializable;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class AlarmEvent implements Serializable {

    @Id
    private long id;
    private String cameraName;
    private long cameraId;
    private String videoPath;
    private String cloudVideoPath;
    private String thumbnailPath;
    private Date date;
    private String message;
    private float detectionConfidence;

    public AlarmEvent(long cameraId, String cameraName, Date date, String message, String videoPath, String thumbnailPath) {
        this.cameraName = cameraName;
        this.videoPath = videoPath;
        this.cameraId = cameraId;
        this.date = date;
        this.message = message;
        this.thumbnailPath = thumbnailPath;
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
}