package com.aiwatch.media;

import com.aiwatch.media.db.CameraConfig;
import java.util.concurrent.ExecutorService;

public class RunningThreadInfo {

    private CameraConfig cameraConfig;
    private ExecutorService executorService;
    private MonitoringRunnable monitoringRunnable;

    public RunningThreadInfo(CameraConfig cameraConfig, ExecutorService executorService, MonitoringRunnable monitoringRunnable) {
        this.cameraConfig = cameraConfig;
        this.executorService = executorService;
        this.monitoringRunnable = monitoringRunnable;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public MonitoringRunnable getMonitoringRunnable() {
        return monitoringRunnable;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
