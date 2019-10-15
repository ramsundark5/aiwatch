package com.aiwatch.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.aiwatch.Logger;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.ai.ObjectDetectionService;
import com.aiwatch.common.AppConstants;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.postprocess.DetectionResultProcessor;
import com.google.firebase.perf.metrics.AddTrace;

import java.io.File;

public class ImageProcessor {

    private static final Logger LOGGER = new Logger();
    private DetectionResultProcessor detectionResultProcessor;
    private ObjectDetectionService objectDetectionService;
    private volatile long previousLastModified;
    private volatile long pauseStartTime;
    private volatile boolean pauseProcessing = false;

    public ImageProcessor(Context context) {
        try {
            this.detectionResultProcessor = new DetectionResultProcessor();
            this.objectDetectionService = new ObjectDetectionService(context.getAssets());
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
    }

    public void processImage(FrameEvent frameEvent){
        try{
            if(!shouldProcess(frameEvent)){
                return;
            }
            ObjectDetectionResult objectDetectionResult = detectImage(frameEvent.getImageFilePath());
            if(objectDetectionResult != null){
                LOGGER.d("detected "+objectDetectionResult.getName() +" at camera "+frameEvent.getCameraConfig().getId());
                pauseProcessing = detectionResultProcessor.isResultInteresting(frameEvent, objectDetectionResult);
                detectionResultProcessor.processObjectDetectionResult(frameEvent, objectDetectionResult);
                if(pauseProcessing){
                    pauseStartTime = System.currentTimeMillis();
                    LOGGER.d("Pause processing for camera " + frameEvent.getCameraConfig().getId());
                    LOGGER.d("Processing will begin after " + getWaitPeriod(frameEvent.getCameraConfig()) + " seconds");
                }
            }
        } catch (Exception e) {
            LOGGER.e(e, "Image process exception ");
        }
    }

    @AddTrace(name = "imageProcessTrace")
    private ObjectDetectionResult detectImage(final String filePath){
        Bitmap bitmapOutput = BitmapFactory.decodeFile(filePath);
        if(bitmapOutput == null){
            return null;
        }
        Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmapOutput, AppConstants.TF_OD_API_INPUT_SIZE, AppConstants.TF_OD_API_INPUT_SIZE, false);
        //conditionally call based on camera config
        final ObjectDetectionResult objectDetectionResult = objectDetectionService.detectObjects(croppedBitmap);
        return objectDetectionResult;
    }

    private boolean shouldProcess(FrameEvent frameEvent){
        boolean isPaused = isProcessingInPause(frameEvent.getCameraConfig());
        boolean imageModified = isImageModified(frameEvent);
        boolean isMonitoringEnabled = frameEvent.getCameraConfig().isMonitoringEnabled();
        return isMonitoringEnabled && !isPaused && imageModified;
    }

    private boolean isProcessingInPause(CameraConfig cameraConfig){
        boolean pause = false;
        if(pauseProcessing){
            long currentTime = System.currentTimeMillis();
            long timeSinceLastProcessing = currentTime - pauseStartTime;
            long elapsedTimeInSeconds = timeSinceLastProcessing/1000;
            long waitPeriod = getWaitPeriod(cameraConfig);
            if(elapsedTimeInSeconds <= waitPeriod){
                pause = true;
            }
            if(!pause){
                pauseProcessing = false;
                pauseStartTime = 0;
                LOGGER.d("Pause is over. Start processing again for camera "+cameraConfig.getId());
            }
        }
        return pause;
    }

    private boolean isImageModified(FrameEvent frameEvent){
        boolean imageModified = false;
        File imageFile = new File(frameEvent.getImageFilePath());
        if(imageFile != null && imageFile.exists()){
            long lastModified = imageFile.lastModified();
            if(lastModified > previousLastModified){
                previousLastModified = lastModified;
                imageModified = true;
            }
        }
        return imageModified;
    }

    private long getWaitPeriod(CameraConfig cameraConfig){
        long waitPeriodInMins = cameraConfig.getWaitPeriodAfterDetection();
        long waitPeriod = waitPeriodInMins * 60;
        if(cameraConfig.isTestModeEnabled() || waitPeriod < 30){
            waitPeriod = AppConstants.WAIT_TIME_AFTER_DETECT;
        }
        return waitPeriod;
    }
}
