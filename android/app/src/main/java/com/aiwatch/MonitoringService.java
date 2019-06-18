package com.aiwatch;

import android.content.Intent;
import android.os.Build;
import com.aiwatch.common.AppConstants;
import com.aiwatch.media.DetectionController;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;

import java.util.List;

public class MonitoringService extends AbstractForegroundService {

    private static final Logger LOGGER = new Logger();

    @Override
    public void onCreate() {
        LOGGER.i("Creating new monitoring service instance. Thread is "+Thread.currentThread().getName());
        startMonitoring();
        //scheduleCompression();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preStart(this);
        String action = intent != null ? intent.getStringExtra(AppConstants.ACTION_EXTRA) : null;
        LOGGER.i("Received onStartCommand with action " + action);
        if(action != null){
            switch (action) {
                case AppConstants.START_MONITORING:
                    startMonitoring();
                    break;
                case AppConstants.STOP_MONITORING:
                    stopMonitoring();
                    break;
                case AppConstants.CONNECT_CAMERA:
                    long connectCameraId = intent.getLongExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, -1);
                    connectCamera(connectCameraId);
                    break;
                case AppConstants.DISCONNECT_CAMERA:
                    long disconnectCameraId = intent.getLongExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, -1);
                    DetectionController.INSTANCE().stopDetecting(disconnectCameraId);
                    break;
                case AppConstants.SAVE_CAMERA:
                    CameraConfig cameraConfig = (CameraConfig) intent.getSerializableExtra(AppConstants.CAMERA_CONFIG_EXTRA);
                    DetectionController.INSTANCE().startDetection(cameraConfig, getApplicationContext());
                    break;
                case AppConstants.REMOVE_CAMERA:
                    long cameraId = intent.getLongExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, -1);
                    DetectionController.INSTANCE().stopDetecting(cameraId);
                    break;
                default:
                    LOGGER.i("unknown command sent to monitoring serivce "+ action);
            }
        }
        return START_STICKY;
    }

    private void connectCamera(long cameraId){
        CameraConfigDao cameraConfigDao = new CameraConfigDao();
        CameraConfig cameraConfig = cameraConfigDao.getCamera(cameraId);
        DetectionController.INSTANCE().startDetection(cameraConfig, getApplicationContext());
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
        }
    }

    private void stopMonitoring(){
        DetectionController.INSTANCE().stopAllDetecting();
        stopSelf();
    }
}
