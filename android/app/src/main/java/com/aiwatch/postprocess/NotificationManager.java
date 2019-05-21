package com.aiwatch.postprocess;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.aiwatch.Logger;
import com.aiwatch.R;
import com.aiwatch.common.AppConstants;
import com.aiwatch.media.FrameEvent;
import com.facebook.react.bridge.UiThreadUtil;
import com.aiwatch.ai.Events;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.media.db.AlarmEvent;
import com.aiwatch.media.db.CameraConfig;

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
            notifyEventToUI(context, alarmEvent);

            UiThreadUtil.runOnUiThread(() -> {
                Toast.makeText(frameEvent.getContext(), alarmEvent.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }catch(Exception e){
            LOGGER.e("Error sending local notification "+e);
        }
    }

    public static void sendStringNotification(FrameEvent frameEvent, String message){
        try{
            Context context = frameEvent.getContext();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(message)
                    .setChannelId(context.getString(R.string.channel_id))
                    //.setContentText(alarmEvent.getMessage())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(frameEvent.hashCode(), builder.build());

            UiThreadUtil.runOnUiThread(() -> {
                Toast.makeText(frameEvent.getContext(), message, Toast.LENGTH_SHORT).show();
            });
        }catch(Exception e){
            LOGGER.e("Error sending local notification "+e);
        }

    }

    public static void sendImageNotification(FrameEvent frameEvent, String message, String imagePath){
        try{
            Context context = frameEvent.getContext();
            Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(message)
                    .setContentText("")
                    .setLargeIcon(imageBitmap)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(imageBitmap)
                            .bigLargeIcon(null))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(frameEvent.hashCode(), builder.build());

            UiThreadUtil.runOnUiThread(() -> {
                Toast toast = Toast.makeText(frameEvent.getContext(), message,Toast.LENGTH_SHORT);
                ImageView imageView = new ImageView(frameEvent.getContext());
                imageView.setImageBitmap(imageBitmap);
                toast.setView(imageView);
                toast.show();
            });
        }catch(Exception e){
            LOGGER.e("Error sending local notification "+e);
        }
    }

    private static void notifyEventToUI(final Context context, final AlarmEvent alarmEvent){
        try{
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
            Intent newAlarmIntent= new Intent(AppConstants.AIWATCH_EVENT_INTENT);
            newAlarmIntent.putExtra(AppConstants.NEW_DETECTION_EVENT, alarmEvent);
            localBroadcastManager.sendBroadcast(newAlarmIntent);
        }catch(Exception e){
            LOGGER.e("error sending event to UI "+e.getMessage());
        }

    }
}
