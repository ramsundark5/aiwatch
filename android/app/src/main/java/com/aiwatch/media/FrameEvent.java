package com.aiwatch.media;
import android.content.Context;
import com.aiwatch.models.CameraConfig;

public class FrameEvent {

    private Context context;
    private CameraConfig cameraConfig;
    private String imageFilePath;

    public FrameEvent(final CameraConfig cameraConfig, final String imageFilePath, final Context context) {
        this.cameraConfig = cameraConfig;
        this.context = context;
        this.imageFilePath = imageFilePath;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public Context getContext() {
        return context;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }
}
