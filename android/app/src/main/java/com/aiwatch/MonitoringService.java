package com.aiwatch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import com.aiwatch.common.AppConstants;
import com.aiwatch.media.DetectionController;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;

import java.util.List;

public class MonitoringService extends Service {

    private static final Logger LOGGER = new Logger();
    private NotificationManager notificationManager;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;
    static volatile PowerManager.WakeLock _wakeLock;
    static volatile WifiManager.WifiLock _wifiLock;
    static boolean isNotifChannelCreated;

    @Override
    public void onCreate() {
        startMonitoring();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGGER.i("Received request to monitoring service", "Received start id " + startId + ": " + intent);
        preStart(this);
        String action = intent.getStringExtra(AppConstants.ACTION_EXTRA);
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

    private void startMonitoring(){
        LOGGER.i("starting monitoring service");
        CameraConfigDao cameraConfigDao = new CameraConfigDao();
        List<CameraConfig> cameraConfigList = cameraConfigDao.getAllCameras();
        if(cameraConfigList == null || cameraConfigList.isEmpty()){
            return;
        }

        for(CameraConfig cameraConfig : cameraConfigList){
            DetectionController.INSTANCE().startDetection(cameraConfig, getApplicationContext());
        }
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    private void stopMonitoring(){
        DetectionController.INSTANCE().stopAllDetecting();
        stopSelf();
    }

    static synchronized void preStart(Context context) {
        synchronized (MonitoringService.class) {
            if (_wakeLock == null) {
                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                _wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MonitoringService.class.getSimpleName());
                _wakeLock.acquire();
            }
            if(_wifiLock == null){
                _wifiLock = ((WifiManager) context.getSystemService(WIFI_SERVICE)).createWifiLock("MonitoringService_wifilock");
                _wifiLock.acquire();
            }
            createNotificationChannel(context);
        }
    }

    static synchronized void postStopCleanup() {
        synchronized (MonitoringService.class) {
            if (_wakeLock != null) {
                _wakeLock.release();
                _wakeLock = null;
            }
            if(_wifiLock != null){
                _wifiLock.release();
                _wifiLock = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        postStopCleanup();
        // Cancel the persistent notification.
        notificationManager.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static void createNotificationChannel(Context context) {
        if (isNotifChannelCreated) {
            return;
        }
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelId = context.getString(R.string.channel_id);
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if (notificationManager.getNotificationChannel(channelId) == null) {
                    CharSequence name = context.getString(R.string.channel_name);
                    String description = context.getString(R.string.channel_description);
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel channel = new NotificationChannel(channelId, name, importance);
                    channel.setDescription(description);
                    notificationManager.createNotificationChannel(channel);
                }
                isNotifChannelCreated = true;
            }
        }catch(Exception e){
            LOGGER.e("Exception creating notification channel "+e.getMessage());
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        try{
            // In this sample, we'll use the same text for the ticker and the expanded notification
            CharSequence text = getText(R.string.local_service_started);

            // The PendingIntent to launch our activity if the user selects this notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);

            // Set the info for the views that show in the notification panel.
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                    .build();

            // Send the notification.
            notificationManager.notify(NOTIFICATION, notification);
        }catch(Exception e){
           LOGGER.e("error showing notification that service started "+e.getMessage());
        }

    }
}
