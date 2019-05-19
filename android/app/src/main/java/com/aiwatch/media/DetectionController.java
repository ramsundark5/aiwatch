package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.media.db.CameraConfig;
import org.bytedeco.javacpp.avcodec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetectionController {

    private static final Logger LOGGER = new Logger();
    private static volatile DetectionController sInstance;
    private static volatile Map<Long, RunningThreadInfo> cameraMap = new ConcurrentHashMap<>();

    private DetectionController() {
    }

    public static DetectionController INSTANCE() {
        if  (sInstance == null) {
            synchronized (DetectionController.class) {
                if (sInstance == null) {
                    sInstance = new DetectionController();
                    cameraMap = new ConcurrentHashMap<>();
                }
            }
        }
        return sInstance;
    }

    public void startDetection(CameraConfig cameraConfig, final Context context) {
        try {
            cameraConfig.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            stopCurrentVideoProcessor(cameraConfig.getId());
            if(!cameraConfig.isMonitoringEnabled()){
                return;
            }
            VideoProcessorRunnable videoProcessorRunnable = new VideoProcessorRunnable(cameraConfig, context);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(videoProcessorRunnable);
            cameraMap.put(cameraConfig.getId(), new RunningThreadInfo(cameraConfig, executorService, videoProcessorRunnable));
            LOGGER.d("Monitoring started for camera "+ cameraConfig.getName() + cameraConfig.getId());
        } catch (Exception e) {
            LOGGER.e("Exception starting detection "+e.getMessage());
        }
    }

    public void stopAllDetecting(){
        if(cameraMap == null && cameraMap.isEmpty()){
            return;
        }
        for( long cameraId : cameraMap.keySet()){
            stopDetecting(cameraId);
        }
    }

    public void stopDetecting(final long cameraId) {
        try {
            stopCurrentVideoProcessor(cameraId);
            LOGGER.d("object detection stopped");
        } catch (Exception e) {
            LOGGER.e("Exception stopping detection "+e.getMessage());
        }
    }

    private void stopCurrentVideoProcessor(long cameraId){
        RunningThreadInfo runningThreadInfo = cameraMap.get(cameraId);
        if(runningThreadInfo != null){
            ExecutorService executorService = runningThreadInfo.getExecutorService();
            VideoProcessorRunnable videoProcessorRunnable = runningThreadInfo.getVideoProcessorRunnable();
            if(videoProcessorRunnable != null){
                videoProcessorRunnable.stop();
            }
            if(executorService != null && !executorService.isShutdown()){
                executorService.shutdown();
            }
            cameraMap.remove(cameraId);
            LOGGER.d("Monitoring started for cameraId "+ runningThreadInfo.getCameraConfig().getName() + runningThreadInfo.getCameraConfig().getId());
        }
    }
}
