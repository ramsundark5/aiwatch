package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.media.db.CameraConfig;
import org.bytedeco.javacpp.avcodec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundServiceManager {

    private static final Logger LOGGER = new Logger();
    private static volatile BackgroundServiceManager sInstance;
    private static volatile Map<Long, BackgroundRecordService> cameraMap = new ConcurrentHashMap<>();

    private BackgroundServiceManager() {
    }

    public static BackgroundServiceManager INSTANCE() {
        if  (sInstance == null) {
            synchronized (BackgroundServiceManager.class) {
                if (sInstance == null) {
                    sInstance = new BackgroundServiceManager();
                    cameraMap = new ConcurrentHashMap<>();
                }
            }
        }
        return sInstance;
    }

    public void startMonitoring(CameraConfig cameraConfig, final Context context) {
        try{
            BackgroundRecordService runningService = cameraMap.get(cameraConfig.getId());
            if(runningService != null){
                runningService.killRecording();
                runningService = null;
            }
            BackgroundRecordService backgroundRecordService = new BackgroundRecordService(context, cameraConfig);
            backgroundRecordService.startFFMpegRecording();
            cameraMap.put(cameraConfig.getId(), backgroundRecordService);
        }catch(Exception e){
            LOGGER.e(e, "error starting recording task");
        }
    }

    public void stopAllMonitoring(){
        if(cameraMap == null && cameraMap.isEmpty()){
            return;
        }
        for( long cameraId : cameraMap.keySet()){
            stopMonitoring(cameraId);
        }
    }

    public void stopMonitoring(long cameraId){
        BackgroundRecordService runningService = cameraMap.get(cameraId);
        if(runningService != null){
            runningService.stopFFMpegRecording();
            runningService = null;
            cameraMap.remove(cameraId);
            LOGGER.d("Monitoring stopped for cameraId "+ cameraId);
        }
    }
}
