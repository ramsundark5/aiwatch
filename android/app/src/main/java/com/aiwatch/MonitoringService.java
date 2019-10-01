package com.aiwatch;

import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.aiwatch.common.AppConstants;
import com.aiwatch.media.DetectionController;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;

import java.util.List;

public class MonitoringService extends AbstractForegroundService {

    private static final Logger LOGGER = new Logger();
    private DetectionController detectionController = new DetectionController();

    private boolean stopMonitoringRequested = false;
    @Override
    public void onCreate() {
        LOGGER.i("Creating new monitoring service instance. Thread is "+Thread.currentThread().getName());
        startForgroundNotification();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable t) {
                LOGGER.e(t, "Uncaught exception "+ t.getMessage());
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preStart(this);
        String action = intent != null ? intent.getStringExtra(AppConstants.ACTION_EXTRA) : null;
        stopMonitoringRequested = false;
        LOGGER.i("Received onStartCommand with action " + action);
        if(action != null){
            switch (action) {
                case AppConstants.START_MONITORING:
                    startMonitoring();
                    break;
                case AppConstants.STOP_MONITORING:
                    stopMonitoringRequested = true;
                    stopMonitoring();
                    break;
                case AppConstants.CONNECT_CAMERA:
                    long connectCameraId = intent.getLongExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, -1);
                    connectCamera(connectCameraId);
                    break;
                case AppConstants.DISCONNECT_CAMERA:
                    long disconnectCameraId = intent.getLongExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, -1);
                    detectionController.stopDetecting(disconnectCameraId);
                    break;
                case AppConstants.SAVE_CAMERA:
                    CameraConfig cameraConfig = (CameraConfig) intent.getSerializableExtra(AppConstants.CAMERA_CONFIG_EXTRA);
                    detectionController.startDetection(cameraConfig, getApplicationContext());
                    break;
                case AppConstants.REMOVE_CAMERA:
                    long cameraId = intent.getLongExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, -1);
                    detectionController.stopDetecting(cameraId);
                    break;
                default:
                    LOGGER.i("unknown command sent to monitoring service "+ action);
            }
        }else{
            //if action is null, its usually because we are creating the service
            startMonitoring();
        }
        return START_STICKY;
    }

    private void connectCamera(long cameraId){
        CameraConfigDao cameraConfigDao = new CameraConfigDao();
        CameraConfig cameraConfig = cameraConfigDao.getCamera(cameraId);
        detectionController.startDetection(cameraConfig, getApplicationContext());
    }

    private void startMonitoring(){
        LOGGER.i("starting monitoring service");
        CameraConfigDao cameraConfigDao = new CameraConfigDao();
        List<CameraConfig> cameraConfigList = cameraConfigDao.getAllCameras();
        if(cameraConfigList == null || cameraConfigList.isEmpty()){
            stopMonitoring();
            return;
        }

        boolean isMonitoringStartedForAny = false;
        for(CameraConfig cameraConfig : cameraConfigList){
            boolean startedForCamera = detectionController.startDetection(cameraConfig, getApplicationContext());
            isMonitoringStartedForAny = isMonitoringStartedForAny || startedForCamera;
        }

        if(!isMonitoringStartedForAny){
            stopMonitoringRequested = true;
            stopMonitoring();
        }
    }

    private void stopMonitoring(){
        detectionController.stopAllDetecting();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if(stopMonitoringRequested){
            LOGGER.d("Stop monitoring requested. Destroying monitoring service");
            postStopCleanup();
            stopForeground(true); //true will remove notification
            Toast.makeText(this, "aiwatch monitoring is stopped", Toast.LENGTH_SHORT).show();
        }
        else{
            Intent monitoringIntent = new Intent(getApplicationContext(), MonitoringService.class);
            startService(monitoringIntent);
            LOGGER.d("Restarted monitoring service");
        }
    }
}
