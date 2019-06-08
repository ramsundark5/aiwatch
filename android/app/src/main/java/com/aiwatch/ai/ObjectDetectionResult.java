package com.aiwatch.ai;

import android.graphics.RectF;

public class ObjectDetectionResult {

    private String name;

    private String message;

    private float confidence;

    /** Optional location within the source image for the location of the recognized object. */
    private RectF location;

    public String getName() {
        return name;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public RectF getLocation() {
        return location;
    }

    public void setLocation(RectF location) {
        this.location = location;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
