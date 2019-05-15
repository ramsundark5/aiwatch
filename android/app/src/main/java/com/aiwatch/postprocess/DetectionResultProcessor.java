package com.aiwatch.postprocess;

import android.graphics.Bitmap;
import android.graphics.RectF;
import com.aiwatch.Logger;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.media.FrameEvent;
import com.aiwatch.media.db.AlarmEvent;
import com.aiwatch.media.db.AlarmEventDao;
import com.aiwatch.media.db.CameraConfig;
import org.bytedeco.javacv.AndroidFrameConverter;
import java.io.FileOutputStream;
import java.util.Date;

public class DetectionResultProcessor {

    private static final Logger LOGGER = new Logger();
    private AndroidFrameConverter frameConverter;
    private AlarmEventDao alarmEventDao;

    public DetectionResultProcessor(){
        this.frameConverter = new AndroidFrameConverter();
        this.alarmEventDao = new AlarmEventDao();
    }

    public boolean processObjectDetectionResult(FrameEvent frameEvent, ObjectDetectionResult objectDetectionResult){
        boolean shouldRecordVideo = RecordingManager.shouldStartRecording(objectDetectionResult, frameEvent.getCameraConfig());
        String videoPath = null;
        if(shouldRecordVideo){
            videoPath = RecordingManager.recordVideo(frameEvent);
        }
        boolean shouldNotify = NotificationManager.shouldNotifyResult(objectDetectionResult, frameEvent.getCameraConfig());
        boolean isResultInteresting = shouldRecordVideo || shouldNotify;
        if(isResultInteresting){
            String thumbnailPath = saveImage(frameEvent, objectDetectionResult);
            CameraConfig cameraConfig = frameEvent.getCameraConfig();
            AlarmEvent alarmEvent = new AlarmEvent(cameraConfig.getId(), cameraConfig.getName(), new Date(), objectDetectionResult.getName(), videoPath, thumbnailPath);
            alarmEvent.setDetectionConfidence(objectDetectionResult.getConfidence());
            alarmEventDao.putEvent(alarmEvent);
            if(shouldNotify){
                NotificationManager.sendNotification(frameEvent, alarmEvent);
            }
        }
        return isResultInteresting;
    }

    private String saveImage(FrameEvent frameEvent, ObjectDetectionResult objectDetectionResult){
        try {
            String imageFilePath = RecordingManager.getFilePathToRecord(frameEvent, ".jpg");
            FileOutputStream fos=new FileOutputStream(imageFilePath);
            Bitmap bitmapOutput = frameConverter.convert(frameEvent.getFrame());
            RectF location = objectDetectionResult.getLocation();
            Bitmap croppedBitmap = Bitmap.createBitmap(
                    bitmapOutput,
                    ((int) location.left),
                    ((int) location.top),
                    ((int) location.width()),
                    ((int) location.height()));
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 128, 128, true);
            //scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            bitmapOutput.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            LOGGER.d("image filepath is "+imageFilePath);
            NotificationManager.sendImageNotification(frameEvent, objectDetectionResult.getName(), imageFilePath);
            return imageFilePath;
        }
        catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
        return null;
    }
}
