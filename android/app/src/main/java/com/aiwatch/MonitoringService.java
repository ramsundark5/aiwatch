package com.aiwatch;

import android.content.Intent;

import com.aiwatch.common.AppConstants;
import com.aiwatch.common.SharedPreferenceUtil;
import com.aiwatch.media.DetectionController;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.postprocess.NotificationManager;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.crashlytics.android.Crashlytics;

import java.util.List;

public class MonitoringService extends AbstractForegroundService {

    private static final Logger LOGGER = new Logger();
    private DetectionController detectionController = DetectionController.INSTANCE();

    @Override
    public void onCreate() {
        LOGGER.i("Creating new monitoring service instance. Thread is "+Thread.currentThread().getName());
        startForgroundNotification();
        Thread.setDefaultUncaughtExceptionHandler((paramThread, t) -> {
            LOGGER.e(t, "Uncaught exception "+ t.getCause().getMessage());
            Crashlytics.log("Uncaught exception "+ t.getCause().getMessage());
            Crashlytics.logException(t);
            //restart monitoring
            startMonitoring();
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preStart(this);
        String action = intent != null ? intent.getStringExtra(AppConstants.ACTION_EXTRA) : null;
        SharedPreferenceUtil.setStopMonitoringRequested(this, false);
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
                    disconnectCamera(disconnectCameraId);
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

    private void disconnectCamera(long cameraId){
        detectionController.stopDetecting(cameraId);

        CameraConfigDao cameraConfigDao = new CameraConfigDao();
        List<CameraConfig> cameraConfigList = cameraConfigDao.getAllCameras();
        if(cameraConfigList == null || cameraConfigList.isEmpty()){
            stopMonitoring();
            return;
        }

        boolean isMonitoringStartedForAny = false;
        for(CameraConfig cameraConfig : cameraConfigList){
            boolean cameraRunning = detectionController.isCameraRunning(cameraConfig.getId());
            isMonitoringStartedForAny = isMonitoringStartedForAny || cameraRunning;
        }
        if(!isMonitoringStartedForAny){
            stopMonitoring();
            return;
        }
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
            stopMonitoring();
            return;
        }

        Config.enableLogCallback(message -> LOGGER.d(message.getText()));
    }

    private void stopMonitoring(){
        SharedPreferenceUtil.setStopMonitoringRequested(this,true);
        detectionController.stopAllDetecting();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if(SharedPreferenceUtil.isStopMonitoringRequested(this)){
            LOGGER.d("Stop monitoring requested. Destroying monitoring service");
            postStopCleanup();
            stopForeground(true); //true will remove notification
            NotificationManager.sendStringNotification(getApplicationContext(), "aiwatch monitoring is stopped");
            //Toast.makeText(this, "aiwatch monitoring is stopped", Toast.LENGTH_SHORT).show();

            //just in case some rogue threads are running, stop everything one more time
            detectionController.stopAllDetecting();
        }
        else{
            Intent monitoringIntent = new Intent(getApplicationContext(), MonitoringService.class);
            startService(monitoringIntent);
            LOGGER.d("Restarted monitoring service");
        }
    }
}
