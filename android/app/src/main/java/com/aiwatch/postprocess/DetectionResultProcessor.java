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
import com.aiwatch.common.FileUtil;
import com.aiwatch.firebase.FirebaseAlarmEventDao;
import com.aiwatch.media.FrameEvent;
import com.aiwatch.models.AlarmEvent;
import com.aiwatch.media.db.AlarmEventDao;
import com.aiwatch.models.CameraConfig;
import com.google.common.net.MediaType;

import org.greenrobot.essentials.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DetectionResultProcessor {

    private static final Logger LOGGER = new Logger();
    private AlarmEventDao alarmEventDao;
    private FirebaseAlarmEventDao firebaseAlarmEventDao;
    private SmartthingsNotificationManager smartthingsNotificationManager;
    private EmailNotificationManager emailNotificationManager;
    private TTSManager ttsManager;

    public DetectionResultProcessor(){
        this.alarmEventDao = new AlarmEventDao();
        this.firebaseAlarmEventDao = new FirebaseAlarmEventDao();
        this.smartthingsNotificationManager = new SmartthingsNotificationManager();
        this.emailNotificationManager = new EmailNotificationManager();
        this.ttsManager = new TTSManager();
    }

    public boolean isResultInteresting(FrameEvent frameEvent, ObjectDetectionResult objectDetectionResult){
        CameraConfig cameraConfig = frameEvent.getCameraConfig();
        boolean withinROI = isWithinROI(cameraConfig, objectDetectionResult.getLocation());
        boolean shouldRecordVideo = RecordingManager.shouldStartRecording(objectDetectionResult, frameEvent.getCameraConfig());
        boolean shouldNotify = NotificationManager.shouldNotifyResult(objectDetectionResult, frameEvent.getCameraConfig());
        boolean isResultInteresting = withinROI && (shouldRecordVideo || shouldNotify);
        return isResultInteresting;
    }

    public boolean processObjectDetectionResult(FrameEvent frameEvent, ObjectDetectionResult objectDetectionResult){
        String videoPath = null;
        String gdriveVideoPath = null;
        String thumbnailPath = null;
        CameraConfig cameraConfig = frameEvent.getCameraConfig();
        String notificationMessage = objectDetectionResult.getMessage() + " at "+ cameraConfig.getName();
        AlarmEvent alarmEvent = new AlarmEvent(cameraConfig.getId(), cameraConfig.getName(), new Date(), notificationMessage, null, thumbnailPath, UUID.randomUUID().toString());
        alarmEvent.setDetectionConfidence(objectDetectionResult.getConfidence());
        boolean withinROI = isWithinROI(cameraConfig, objectDetectionResult.getLocation());
        boolean shouldRecordVideo = RecordingManager.shouldStartRecording(objectDetectionResult, frameEvent.getCameraConfig());
        boolean shouldNotify = NotificationManager.shouldNotifyResult(objectDetectionResult, frameEvent.getCameraConfig());
        boolean isResultInteresting = withinROI && (shouldRecordVideo || shouldNotify);
        if(isResultInteresting){
            thumbnailPath = saveImage(frameEvent, objectDetectionResult);
            alarmEvent.setThumbnailPath(thumbnailPath);
        }
        if(shouldNotify){
            //first notify the event
            if(cameraConfig.isTtsEnabled()){
                ttsManager.speakMessage(frameEvent.getContext(), notificationMessage);
            }
            emailNotificationManager.sendEmail(alarmEvent);
            smartthingsNotificationManager.notifyHub();
            AlexaNotificationManager.notifyAlexa(alarmEvent);
            String gdriveImagePath = RecordingManager.saveToGdrive(frameEvent.getContext(), frameEvent.getCameraConfig().getId(), thumbnailPath, MediaType.PNG.toString(), RecordingManager.DEFAULT_IMAGE_EXTENSION);
            alarmEvent.setCloudImagePath(gdriveImagePath);
            NotificationManager.sendImageNotification(frameEvent.getContext(), alarmEvent);
        }
        //now record
        if(shouldRecordVideo){
            int recordingDuration = cameraConfig.getRecordingDuration();
            if(recordingDuration < 10){
                recordingDuration = 15;
            }
            LOGGER.d("recording duration is "+recordingDuration);
            long waitTimeForRecording = TimeUnit.SECONDS.toMillis(recordingDuration + 2);
            try {
                Thread.sleep(waitTimeForRecording);
            } catch (InterruptedException e) {
               LOGGER.e(e, "Error trying to wait till recording finishes");
            }
            videoPath = RecordingManager.recordToLocal(frameEvent, alarmEvent);
            if(videoPath != null){
                gdriveVideoPath = RecordingManager.saveToGdrive(frameEvent.getContext(), frameEvent.getCameraConfig().getId(), videoPath, MediaType.MP4_VIDEO.toString(), RecordingManager.DEFAULT_VIDEO_EXTENSION);
            }
        }

        if(isResultInteresting){
            //store the results
            alarmEvent.setVideoPath(videoPath);
            alarmEvent.setCloudVideoPath(gdriveVideoPath);
            alarmEventDao.putEvent(alarmEvent);
            //this will allow UI redux store to refresh with latest results
            NotificationManager.sendUINotification(frameEvent.getContext(), alarmEvent);
            firebaseAlarmEventDao.addEvent(frameEvent.getContext(), alarmEvent);
        }
        return isResultInteresting;
    }

    private String saveImage(FrameEvent frameEvent, ObjectDetectionResult objectDetectionResult){
        String outputFilePath = null;
        try {
            String inputFilePath = frameEvent.getImageFilePath();
            File baseDirectory = FileUtil.getBaseDirectory(frameEvent.getContext(), AppConstants.EVENT_IMAGES_FOLDER);
            outputFilePath = RecordingManager.getFilePathToRecord(frameEvent.getCameraConfig().getId(), baseDirectory, ".png");
            FileUtils.copyFile(inputFilePath, outputFilePath);
            RectF location = objectDetectionResult.getLocation();

            //draw bounding boxes
            if(location != null){
                try{
                    drawBoundingBox(outputFilePath, location);
                }catch(Exception e){
                    LOGGER.e(e, "Error drawing bounding boxes");
                    //restore the original file
                    FileUtils.copyFile(inputFilePath, outputFilePath);
                }
            }
            LOGGER.d("image filepath is " + outputFilePath);
        }
        catch (Exception e) {
            LOGGER.e(e, e.getMessage());
        }
        return outputFilePath;
    }

    private void drawBoundingBox(final String outputFilePath, final RectF location) throws Exception {
        Bitmap bitmapOutput = BitmapFactory.decodeFile(outputFilePath).copy(Bitmap.Config.ARGB_8888, true);
        final Canvas canvas = new Canvas(bitmapOutput);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        canvas.drawRect(location, paint);

        FileOutputStream fos=new FileOutputStream(outputFilePath);
        bitmapOutput.compress(Bitmap.CompressFormat.PNG, 90, fos);
        fos.flush();
        fos.close();
    }

    private boolean isWithinROI(final CameraConfig cameraConfig, final RectF location){
        if(cameraConfig.isTestModeEnabled()){
            return true;
        }
        if(location == null){
            return false;
        }
        RectF roi = new RectF(cameraConfig.getTopLeftX(), cameraConfig.getTopLeftY(), cameraConfig.getBottomRightX(), cameraConfig.getBottomRightY());
        boolean intersect = roi.intersect(location);
        return intersect;
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
