package com.aiwatch.postprocess;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.widget.Toast;

import com.aiwatch.Logger;
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

    public static void sendUINotification(FrameEvent frameEvent, AlarmEvent alarmEvent){
        try{
            Context context = frameEvent.getContext();
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
            Intent newAlarmIntent= new Intent(AppConstants.AIWATCH_EVENT_INTENT);
            newAlarmIntent.putExtra(AppConstants.NEW_DETECTION_EVENT, alarmEvent);
            localBroadcastManager.sendBroadcast(newAlarmIntent);

            UiThreadUtil.runOnUiThread(() -> {
                Toast.makeText(frameEvent.getContext(), alarmEvent.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }catch(Exception e){
            LOGGER.e(e, "Error sending local notification "+e);
        }
    }

    public static void sendUINotification(Context context, CameraConfig cameraConfig){
        try{
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
            Intent newAlarmIntent= new Intent(AppConstants.AIWATCH_EVENT_INTENT);
            newAlarmIntent.putExtra(AppConstants.STATUS_CHANGED_EVENT, cameraConfig);
            localBroadcastManager.sendBroadcast(newAlarmIntent);

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
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(message)
                    .setChannelId(context.getString(R.string.channel_id))
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
            Bitmap imageBitmap = BitmapFactory.decodeFile(alarmEvent.getThumbnailPath());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(alarmEvent.getMessage())
                    .setContentText("")
                    .setLargeIcon(imageBitmap)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(imageBitmap)
                            .bigLargeIcon(null))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(alarmEvent.hashCode(), builder.build());
/*
            UiThreadUtil.runOnUiThread(() -> {
                Toast toast = Toast.makeText(context, alarmEvent.getMessage(), Toast.LENGTH_SHORT);
                ImageView imageView = new ImageView(context);
                imageView.setImageBitmap(imageBitmap);
                toast.setView(imageView);
                toast.show();
            });*/

            //send remote push notification
            FirebaseNotificationDao firebaseNotificationDao = new FirebaseNotificationDao();
            firebaseNotificationDao.sendPushNotification(context, alarmEvent);
        }catch(Exception e){
            LOGGER.e(e, "Error sending local notification "+e);
        }
    }
}
