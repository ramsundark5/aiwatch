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
import androidx.core.app.NotificationCompat;
import android.widget.Toast;

public abstract class AbstractForegroundService extends Service {

    private static final Logger LOGGER = new Logger();
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION_ID = R.string.local_service_started;
    static volatile PowerManager.WakeLock _wakeLock;
    static volatile WifiManager.WifiLock _wifiLock;
    static boolean isNotifChannelCreated;

    static synchronized void preStart(Context context) {
        try{
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
            }
        }catch(Exception e){
            LOGGER.e(e, "Error acquiring wifilock or wakelock");
        }

    }

    static synchronized void postStopCleanup() {
        try{
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
        }catch(Exception e){
            LOGGER.e(e, "Error releasing wifilock or wakelock");
        }

    }

    @Override
    public void onDestroy() {
        postStopCleanup();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); //true will remove notification
        }
        Toast.makeText(this, "aiwatch monitoring is stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Show a notification while this service is running.
     */
    public void startForgroundNotification(){
        try {
            if (!isNotifChannelCreated) {
                createNotificationChannel(getApplicationContext());
                isNotifChannelCreated = true;
            }
            // The PendingIntent to launch our activity if the user selects this notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);
            String NOTIFICATION_CHANNEL_ID = getApplicationContext().getString(R.string.channel_id);
            // Set the info for the views that show in the notification panel.
            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)  // the status icon
                    .setTicker("aiwatch monitoring started")  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                    .setContentText("aiwatch monitoring is active and running")  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                    .build();
            startForeground(NOTIFICATION_ID, notification);
        }catch(Exception e){
            LOGGER.e("Exception starting foregorund notification "+e.getMessage());
        }

    }

    private void createNotificationChannel(Context context) {
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
}
