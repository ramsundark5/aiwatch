package com.aiwatch.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.aiwatch.Logger;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.ai.ObjectDetectionService;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.common.AppConstants;
import com.aiwatch.postprocess.DetectionResultProcessor;
import com.google.firebase.perf.metrics.AddTrace;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


public class MonitoringRunnable implements Runnable {

    private static final Logger LOGGER = new Logger();
    private CameraConfig cameraConfig;
    private DetectionResultProcessor detectionResultProcessor;
    private ObjectDetectionService objectDetectionService;
    private Context context;
    private FFmpegFrameExtractor fFmpegFrameExtractor;
    private String imageFilePath;
    private Timer ffmpegTimer = new Timer("ffmpegCheckTimer");
    private Timer imageProcessTimer = new Timer("imageProcessTimer");
    private long previousLastModified;

    public MonitoringRunnable(CameraConfig cameraConfig, Context context) {
        try {
            this.cameraConfig = cameraConfig;
            this.context = context;
            this.detectionResultProcessor = new DetectionResultProcessor();
            this.fFmpegFrameExtractor = new FFmpegFrameExtractor(context, cameraConfig);
            this.objectDetectionService = new ObjectDetectionService(context.getAssets());
            this.imageFilePath = getImageFilePath();
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
    }

    public void stop() {
        LOGGER.i("monitoring stop requested for camera "+cameraConfig.getId());
        fFmpegFrameExtractor.stop();
        ffmpegTimer.cancel();
        imageProcessTimer.cancel();
    }

    @Override
    public void run() {
        try {
            LOGGER.i("Creating new VideoProcessor runnable instance. Thread is "+Thread.currentThread().getName());
            fFmpegFrameExtractor.start(imageFilePath);
            startFFmpegCheckTimer();
            startImageProcessTimer();
        } catch (Exception e) {
            LOGGER.e(e, "monitoring exception ");
        }
    }

    private void processImage(final String file){
        try{
            ObjectDetectionResult objectDetectionResult = detectImage(file);
            if(objectDetectionResult != null){
                LOGGER.d("detected "+objectDetectionResult.getName());
                FrameEvent frameEvent = new FrameEvent(cameraConfig, imageFilePath, context);
                detectionResultProcessor.processObjectDetectionResult(frameEvent, objectDetectionResult);
            }
        } catch (Exception e) {
            LOGGER.e(e, "Image process exception ");
        }
    }

    @AddTrace(name = "imageProcessTrace")
    public ObjectDetectionResult detectImage(final String filePath){
        Bitmap bitmapOutput = BitmapFactory.decodeFile(filePath);
        if(bitmapOutput == null){
            return null;
        }
        Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmapOutput, AppConstants.TF_OD_API_INPUT_SIZE, AppConstants.TF_OD_API_INPUT_SIZE, false);
        //conditionally call based on camera config
        final ObjectDetectionResult objectDetectionResult = objectDetectionService.detectObjects(croppedBitmap);
        return objectDetectionResult;
    }

    private String getImageFilePath(){
        File imageFolder = new File(context.getFilesDir(), AppConstants.IMAGES_FOLDER);
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }
        String imageFolderPath = imageFolder.getAbsolutePath();
        File imageFile = new File(imageFolderPath, "/" + cameraConfig.getId() + "-camera.png");
        return imageFile.getAbsolutePath();
    }

    private void startFFmpegCheckTimer(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(!fFmpegFrameExtractor.isRunning()){
                    fFmpegFrameExtractor.start(imageFilePath);
                }
            }
        };

        ffmpegTimer.schedule(timerTask, 60000, 30000);
    }

    public void startImageProcessTimer(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try{
                    File imageFile = new File(imageFilePath);
                    if(imageFile != null && imageFile.exists()){
                        long lastModified = imageFile.lastModified();
                        if(lastModified > previousLastModified){
                            processImage(imageFilePath);
                            previousLastModified = lastModified;
                        }
                    }
                }catch(Exception e){
                    LOGGER.e(e, "Error starting image processing");
                }
            }
        };

        imageProcessTimer.schedule(timerTask, 10000, 1000);
    }
}
