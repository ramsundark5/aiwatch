package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.models.CameraConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DetectionController {

    private static final Logger LOGGER = new Logger();
    private  Map<Long, RunningThreadInfo> cameraMap;

    public DetectionController() {
        cameraMap = new ConcurrentHashMap<>();
    }

    public synchronized boolean startDetection(CameraConfig cameraConfig, final Context context) {
        try {
            if(!cameraConfig.isMonitoringEnabled() && !cameraConfig.isCvrEnabled() && !cameraConfig.isLiveHLSViewEnabled()){
                LOGGER.d("camera monitoring not started because isMonitoringEnabled, isCvrEnabled and isLiveHLSViewEnabled flag is false");
                return false;
            }
            //stop the ffmpeg only if monitoring or cvr is enabled
            if(cameraConfig.isMonitoringEnabled() || cameraConfig.isCvrEnabled()){
                stopSelectedVideoProcessor(cameraConfig.getId());
            }else if(cameraConfig.isLiveHLSViewEnabled()){
                //only isLiveHLSViewEnabled. don't restart the service if its already running
                RunningThreadInfo runningThreadInfo = cameraMap.get(cameraConfig.getId());
                if(runningThreadInfo != null){
                    return false;
                }
            }

            MonitoringRunnable monitoringRunnable = new MonitoringRunnable(cameraConfig, context);
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            cameraMap.put(cameraConfig.getId(), new RunningThreadInfo(cameraConfig, executorService, monitoringRunnable));
            executorService.schedule(monitoringRunnable, 1, TimeUnit.SECONDS);
            LOGGER.d("Monitoring started for camera "+ cameraConfig.getName() + cameraConfig.getId());
        } catch (Exception e) {
            LOGGER.e("Exception starting detection "+e.getMessage());
        }
        return true;
    }

    public synchronized void stopAllDetecting(){
        if(cameraMap == null && cameraMap.isEmpty()){
            return;
        }
        for( long cameraId : cameraMap.keySet()){
            stopDetecting(cameraId);
        }
    }

    public synchronized void stopDetecting(final long cameraId) {
        try {
            stopSelectedVideoProcessor(cameraId);
            LOGGER.d("object detection stopped");
        } catch (Exception e) {
            LOGGER.e("Exception stopping detection "+e.getMessage());
        }
    }

    private synchronized void stopSelectedVideoProcessor(long cameraId){
        RunningThreadInfo runningThreadInfo = cameraMap.get(cameraId);
        if(runningThreadInfo != null){
            ExecutorService executorService = runningThreadInfo.getExecutorService();
            MonitoringRunnable monitoringRunnable = runningThreadInfo.getMonitoringRunnable();
            if(monitoringRunnable != null){
                monitoringRunnable.stop();
            }
            if(executorService != null && !executorService.isShutdown()){
                executorService.shutdown();
            }
            cameraMap.remove(cameraId);
            LOGGER.d("Monitoring stopped for cameraId "+ runningThreadInfo.getCameraConfig().getName() + runningThreadInfo.getCameraConfig().getId());
        }else{
            cameraMap.remove(cameraId);
        }
    }
}
