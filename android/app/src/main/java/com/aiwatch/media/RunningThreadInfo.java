package com.aiwatch.media;

import com.aiwatch.media.db.CameraConfig;
import java.util.concurrent.ExecutorService;

public class RunningThreadInfo {

    private CameraConfig cameraConfig;
    private ExecutorService executorService;
    private VideoProcessorRunnable videoProcessorRunnable;

    public RunningThreadInfo(CameraConfig cameraConfig, ExecutorService executorService, VideoProcessorRunnable videoProcessorRunnable) {
        this.cameraConfig = cameraConfig;
        this.executorService = executorService;
        this.videoProcessorRunnable = videoProcessorRunnable;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public VideoProcessorRunnable getVideoProcessorRunnable() {
        return videoProcessorRunnable;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
