package com.aiwatch.postprocess;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.widget.Toast;

import com.aiwatch.Logger;
import com.aiwatch.MainActivity;
import com.aiwatch.R;
import com.aiwatch.common.AppConstants;
import com.aiwatch.firebase.FirebaseNotificationDao;
import com.aiwatch.media.FrameEvent;
import com.facebook.react.bridge.UiThreadUtil;
import com.aiwatch.ai.Events;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.models.AlarmEvent;
import com.aiwatch.models.CameraConfig;

public class NotificationManager {

    private static final Logger LOGGER = new Logger();

    public static boolean shouldNotifyResult(ObjectDetectionResult objectDetectionResult, CameraConfig cameraConfig){
        //conditionally notify based on camera config
        boolean shouldNotify = false;
        if(Events.PERSON_DETECTED_EVENT.getName().equals(objectDetectionResult.getName()) && cameraConfig.isNotifyPersonDetect()){
            shouldNotify = true;
        }else if(Events.ANIMAL_DETECTED_EVENT.getName().equals(objectDetectionResult.getName()) && cameraConfig.isNotifyAnimalDetect()){
            shouldNotify = true;
        }else if(Events.VEHICLE_DETECTED_EVENT.getName().equals(objectDetectionResult.getName()) && cameraConfig.isNotifyVehicleDetect()){
            shouldNotify = true;
        }else if(cameraConfig.isTestModeEnabled()){
            shouldNotify = true;
        }
        return shouldNotify;
    }

    public static void sendUINotification(Context context, AlarmEvent alarmEvent){
        try{
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
            Intent newAlarmIntent= new Intent(AppConstants.AIWATCH_EVENT_INTENT);
            newAlarmIntent.putExtra(AppConstants.NEW_DETECTION_EVENT, alarmEvent);
            localBroadcastManager.sendBroadcast(newAlarmIntent);

            UiThreadUtil.runOnUiThread(() -> {
                Toast.makeText(context, alarmEvent.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }catch(Exception e){
            LOGGER.e(e, "Error sending local notification "+e);
        }
    }

    public static void sendUINotification(Context context, CameraConfig cameraConfig){
        try{
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
            Intent cameraConfigEvent= new Intent(AppConstants.AIWATCH_EVENT_INTENT);
            cameraConfigEvent.putExtra(AppConstants.STATUS_CHANGED_EVENT, cameraConfig);
            localBroadcastManager.sendBroadcast(cameraConfigEvent);

            String connectedMessage = "Camera  "+cameraConfig.getName() + " connected.";
            String disconnectedMessage = "Camera  "+cameraConfig.getName() + " disconnected.";
            final String message = cameraConfig.isDisconnected() ? disconnectedMessage : connectedMessage;

            UiThreadUtil.runOnUiThread(() -> {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            });
        }catch(Exception e){
            LOGGER.e(e, "Error sending local notification "+e);
        }
    }

    public static void sendStringNotification(Context context, String message){
        sendStringNotification(context, message, 0);
    }

    public static void sendStringNotification(Context context, String message, int notificationIcon){
        try{
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            String NOTIFICATION_CHANNEL_ID = context.getString(R.string.channel_id);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(message)
                    .setContentIntent(contentIntent)
                    //.setContentText(alarmEvent.getMessage())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            if(notificationIcon != 0){
                Bitmap imageBitmap = BitmapFactory.decodeResource(context.getResources(), notificationIcon);
                builder.setLargeIcon(imageBitmap);
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(message.hashCode(), builder.build());

            UiThreadUtil.runOnUiThread(() -> {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            });
        }catch(Exception e){
            LOGGER.e(e, "Error sending local notification "+e);
        }
    }

    public static void sendImageNotification(Context context, AlarmEvent alarmEvent){
        try{
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            String NOTIFICATION_CHANNEL_ID = context.getString(R.string.channel_id);
            Bitmap imageBitmap = BitmapFactory.decodeFile(alarmEvent.getThumbnailPath());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(alarmEvent.getMessage())
                    .setContentText("")
                    .setContentIntent(contentIntent)
                    .setLargeIcon(imageBitmap)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(imageBitmap)
                            .bigLargeIcon(null))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(alarmEvent.hashCode(), builder.build());

            //send remote push notification
            FirebaseNotificationDao firebaseNotificationDao = new FirebaseNotificationDao();
            firebaseNotificationDao.sendPushNotification(context, alarmEvent);
        }catch(Exception e){
            LOGGER.e(e, "Error sending local notification "+e);
        }
    }
}
