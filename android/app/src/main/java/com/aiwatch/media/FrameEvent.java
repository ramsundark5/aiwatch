package com.aiwatch.media;
import android.content.Context;
import com.aiwatch.media.db.CameraConfig;

import org.bytedeco.javacv.Frame;

public class FrameEvent {

    private Frame frame;
    private Context context;
    private CameraConfig cameraConfig;

    public FrameEvent(final Frame frame, final CameraConfig cameraConfig, final Context context) {
        this.frame = frame;
        this.cameraConfig = cameraConfig;
        this.context = context;
    }

    public Frame getFrame() {
        return frame;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public Context getContext() {
        return context;
    }
}
