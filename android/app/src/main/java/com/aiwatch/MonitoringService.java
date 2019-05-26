package com.aiwatch;

import android.content.Intent;
import android.os.Build;
import com.aiwatch.common.AppConstants;
import com.aiwatch.media.CompressionRunnable;
import com.aiwatch.media.DetectionController;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;

import java.util.List;

public class MonitoringService extends AbstractForegroundService {

    private static final Logger LOGGER = new Logger();
    static Thread compressionThread;

    @Override
    public void onCreate() {
        LOGGER.i("Creating new monitoring service instance ");
        startMonitoring();
        scheduleCompression();
    }

    public void scheduleCompression(){
        try {
            boolean isCompressionThreadRunning = compressionThread != null
                    && compressionThread.isAlive();
            if(!isCompressionThreadRunning){
                CompressionRunnable compressionRunnable = new CompressionRunnable(getApplicationContext());
                compressionThread = new Thread(compressionRunnable);
                compressionThread.start();
            }
        } catch (Exception e) {
            LOGGER.e(e, "compression exception " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preStart(this);
        String action = intent.getStringExtra(AppConstants.ACTION_EXTRA);
        LOGGER.i("Received onStartCommand with action " + action);
        if(action != null){
            switch (action) {
                case AppConstants.START_MONITORING:
                    startMonitoring();
                    break;
                case AppConstants.STOP_MONITORING:
                    stopMonitoring();
                    break;
                case AppConstants.SAVE_CAMERA:
                    CameraConfig cameraConfig = (CameraConfig) intent.getSerializableExtra(AppConstants.CAMERA_CONFIG_EXTRA);
                    DetectionController.INSTANCE().startDetection(cameraConfig, getApplicationContext());
                    //BackgroundServiceManager.INSTANCE().startMonitoring(cameraConfig, getApplicationContext());
                    break;
                case AppConstants.REMOVE_CAMERA:
                    long cameraId = intent.getLongExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, -1);
                    DetectionController.INSTANCE().stopDetecting(cameraId);
                    //BackgroundServiceManager.INSTANCE().stopMonitoring(cameraId);
                    break;
                default:
                    LOGGER.i("unknown command sent to monitoring serivce "+ action);
            }
        }
        return START_STICKY;
    }

    private void startMonitoring(){
        LOGGER.i("starting monitoring service");
        CameraConfigDao cameraConfigDao = new CameraConfigDao();
        List<CameraConfig> cameraConfigList = cameraConfigDao.getAllCameras();
        if(cameraConfigList == null || cameraConfigList.isEmpty()){
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForgroundNotification();
        }

        for(CameraConfig cameraConfig : cameraConfigList){
            DetectionController.INSTANCE().startDetection(cameraConfig, getApplicationContext());
            //BackgroundServiceManager.INSTANCE().startMonitoring(cameraConfig, getApplicationContext());
        }
    }

    private void stopMonitoring(){
        DetectionController.INSTANCE().stopAllDetecting();
        //BackgroundServiceManager.INSTANCE().stopAllMonitoring();
        stopSelf();
    }
}
