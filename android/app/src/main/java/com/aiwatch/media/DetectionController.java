package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.media.db.CameraConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetectionController {

    private static final Logger LOGGER = new Logger();
    private  Map<Long, RunningThreadInfo> cameraMap;

    public DetectionController() {
        cameraMap = new ConcurrentHashMap<>();
    }

    public synchronized boolean startDetection(CameraConfig cameraConfig, final Context context) {
        try {
            //cameraConfig.setVideoCodec(AV_CODEC_ID_H264);
            stopSelectedVideoProcessor(cameraConfig.getId());
            if(!cameraConfig.isMonitoringEnabled()){
                return false;
            }
            MonitoringRunnable monitoringRunnable = new MonitoringRunnable(cameraConfig, context);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(monitoringRunnable);
            cameraMap.put(cameraConfig.getId(), new RunningThreadInfo(cameraConfig, executorService, monitoringRunnable));
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
