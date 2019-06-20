package com.aiwatch.media;
import android.content.Context;
import com.aiwatch.media.db.CameraConfig;

public class FrameEvent {

    private Context context;
    private CameraConfig cameraConfig;

    public FrameEvent(final CameraConfig cameraConfig, final Context context) {
        this.cameraConfig = cameraConfig;
        this.context = context;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public Context getContext() {
        return context;
    }
}
