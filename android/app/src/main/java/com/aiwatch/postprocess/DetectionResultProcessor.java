package com.aiwatch.postprocess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import com.aiwatch.Logger;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.common.AppConstants;
import com.aiwatch.firebase.FirebaseAlarmEventDao;
import com.aiwatch.media.FrameEvent;
import com.aiwatch.media.db.AlarmEvent;
import com.aiwatch.media.db.AlarmEventDao;
import com.aiwatch.media.db.CameraConfig;
import com.google.common.net.MediaType;

import org.greenrobot.essentials.io.FileUtils;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.UUID;

public class DetectionResultProcessor {

    private static final Logger LOGGER = new Logger();
    private AlarmEventDao alarmEventDao;
    private FirebaseAlarmEventDao firebaseAlarmEventDao;

    public DetectionResultProcessor(){
        this.alarmEventDao = new AlarmEventDao();
        this.firebaseAlarmEventDao = new FirebaseAlarmEventDao();
    }

    public boolean processObjectDetectionResult(FrameEvent frameEvent, ObjectDetectionResult objectDetectionResult){
        String videoPath = null;
        String gdriveVideoPath = null;
        String thumbnailPath = null;
        CameraConfig cameraConfig = frameEvent.getCameraConfig();
        String notificationMessage = objectDetectionResult.getMessage() + " at "+ cameraConfig.getName();
        AlarmEvent alarmEvent = new AlarmEvent(cameraConfig.getId(), cameraConfig.getName(), new Date(), notificationMessage, null, thumbnailPath, UUID.randomUUID().toString());
        alarmEvent.setDetectionConfidence(objectDetectionResult.getConfidence());
        boolean shouldRecordVideo = RecordingManager.shouldStartRecording(objectDetectionResult, frameEvent.getCameraConfig());
        boolean shouldNotify = NotificationManager.shouldNotifyResult(objectDetectionResult, frameEvent.getCameraConfig());
        boolean isResultInteresting = shouldRecordVideo || shouldNotify;
        if(isResultInteresting){
            thumbnailPath = saveImage(frameEvent, objectDetectionResult);
        }
        if(shouldNotify){
            //first notify the event
            alarmEvent.setThumbnailPath(thumbnailPath);
            String gdriveImagePath = RecordingManager.saveToGdrive(frameEvent.getContext(), frameEvent.getCameraConfig().getId(), thumbnailPath, MediaType.PNG.toString(), RecordingManager.DEFAULT_IMAGE_EXTENSION);
            alarmEvent.setCloudImagePath(gdriveImagePath);
            NotificationManager.sendImageNotification(frameEvent.getContext(), alarmEvent);
        }
        //now record
        if(shouldRecordVideo){
            long waitTimeForRecording = cameraConfig.getRecordingDuration() + AppConstants.PRE_RECORDING_BUFFER;
            try {
                Thread.sleep(waitTimeForRecording);
            } catch (InterruptedException e) {
               LOGGER.e(e, "Error trying to wait till recording finishes");
            }
            videoPath = RecordingManager.recordToLocal(frameEvent);
            gdriveVideoPath = RecordingManager.saveToGdrive(frameEvent.getContext(), frameEvent.getCameraConfig().getId(), videoPath, MediaType.MP4_VIDEO.toString(), RecordingManager.DEFAULT_VIDEO_EXTENSION);
        }

        if(isResultInteresting){
            //store the results
            alarmEvent.setVideoPath(videoPath);
            alarmEvent.setCloudVideoPath(gdriveVideoPath);
            alarmEventDao.putEvent(alarmEvent);
            //this will allow UI redux store to refresh with latest results
            NotificationManager.sendUINotification(frameEvent, alarmEvent);
            firebaseAlarmEventDao.addEvent(frameEvent.getContext(), alarmEvent);
        }
        return isResultInteresting;
    }

    private String saveImage(FrameEvent frameEvent, ObjectDetectionResult objectDetectionResult){
        try {
            String inputFilePath = frameEvent.getImageFilePath();
            String outputFilePath = RecordingManager.getFilePathToRecord(frameEvent, ".jpg");
            FileUtils.copyFile(inputFilePath, outputFilePath);
            RectF location = objectDetectionResult.getLocation();
            if(location != null){
                FileOutputStream fos=new FileOutputStream(outputFilePath);
                Bitmap bitmapOutput = BitmapFactory.decodeFile(outputFilePath);
                //if bounding box needed, comment out the above line and uncomment the below ones
                drawBoundingBox(bitmapOutput, location);
                bitmapOutput.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                fos.close();
            }
            LOGGER.d("image filepath is " + outputFilePath);
            return outputFilePath;
        }
        catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
        return null;
    }

    private void drawBoundingBox(Bitmap bitmap, RectF location){
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        canvas.drawRect(location, paint);
    }

    private Bitmap cropBitmap(Bitmap bitmapOutput, RectF location){
        Bitmap croppedBitmap = Bitmap.createBitmap(
                        bitmapOutput,
                        ((int) location.left),
                        ((int) location.top),
                        ((int) location.width()),
                        ((int) location.height()));
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 128, 128, true);
        return croppedBitmap;
    }
}
