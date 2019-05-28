package com.aiwatch.postprocess;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TimingLogger;

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
        String gdriveVideoPath = null;
        boolean shouldNotify = NotificationManager.shouldNotifyResult(objectDetectionResult, frameEvent.getCameraConfig());
        if(shouldNotify){
            //first notify the event
            NotificationManager.sendStringNotification(frameEvent, objectDetectionResult.getName());
        }
        //now record
        if(shouldRecordVideo){
            videoPath = RecordingManager.recordToLocal(frameEvent);
            gdriveVideoPath = RecordingManager.recordToGdrive(frameEvent, videoPath);
        }
        boolean isResultInteresting = shouldRecordVideo || shouldNotify;

        if(isResultInteresting){
            String thumbnailPath = saveImage(frameEvent, objectDetectionResult);
            //store the results
            CameraConfig cameraConfig = frameEvent.getCameraConfig();
            AlarmEvent alarmEvent = new AlarmEvent(cameraConfig.getId(), cameraConfig.getName(), new Date(), objectDetectionResult.getName(), videoPath, thumbnailPath);
            alarmEvent.setDetectionConfidence(objectDetectionResult.getConfidence());
            alarmEvent.setCloudVideoPath(gdriveVideoPath);
            alarmEventDao.putEvent(alarmEvent);

            //this will allow UI redux store to refresh with latest results
            NotificationManager.sendUINotification(frameEvent, alarmEvent);
        }
        return isResultInteresting;
    }

    private String saveImage(FrameEvent frameEvent, ObjectDetectionResult objectDetectionResult){
        try {
            String imageFilePath = RecordingManager.getFilePathToRecord(frameEvent, ".jpg");
            FileOutputStream fos=new FileOutputStream(imageFilePath);
            TimingLogger timings = new TimingLogger(LOGGER.DEFAULT_TAG, "Frame converter performance");
            Bitmap bitmapOutput = frameConverter.convert(frameEvent.getFrame());
            timings.dumpToLog();
            RectF location = objectDetectionResult.getLocation();
            if(location != null){
                drawBoundingBox(bitmapOutput, location);
                /*Bitmap croppedBitmap = Bitmap.createBitmap(
                        bitmapOutput,
                        ((int) location.left),
                        ((int) location.top),
                        ((int) location.width()),
                        ((int) location.height()));
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 128, 128, true);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);*/
                bitmapOutput.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
            }else{
                bitmapOutput.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
            }
            LOGGER.d("image filepath is "+imageFilePath);
            NotificationManager.sendImageNotification(frameEvent, objectDetectionResult.getName(), imageFilePath);
            return imageFilePath;
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
}
