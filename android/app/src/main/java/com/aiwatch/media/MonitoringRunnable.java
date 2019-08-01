package com.aiwatch.media;

import android.content.Context;

import com.aiwatch.Logger;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.common.AppConstants;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class MonitoringRunnable implements Runnable {

    private static final Logger LOGGER = new Logger();
    private CameraConfig cameraConfig;
    private Context context;
    private FFmpegFrameExtractor ffmpegFrameExtractor;
    private ImageProcessor imageProcessor;
    private String imageFilePath;
    private OldMediaCleaner oldMediaCleaner;
    private Timer ffmpegTimer = new Timer("ffmpegCheckTimer");
    private Timer imageProcessTimer = new Timer("imageProcessTimer");
    private AtomicBoolean running = new AtomicBoolean(false);

    public MonitoringRunnable(CameraConfig cameraConfig, Context context) {
        try {
            this.cameraConfig = cameraConfig;
            this.context = context;
            this.ffmpegFrameExtractor = new FFmpegFrameExtractor(context, cameraConfig);
            this.imageFilePath = getImageFilePath();
            this.imageProcessor = new ImageProcessor(context);
            this.oldMediaCleaner = new OldMediaCleaner();
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
    }

    public void stop() {
        running.set(false);
        LOGGER.i("monitoring stop requested for camera "+cameraConfig.getId());
        ffmpegFrameExtractor.stop();
        ffmpegTimer.cancel();
        imageProcessTimer.cancel();
    }

    @Override
    public void run() {
        try {
            running.set(true);
            LOGGER.i("Creating new VideoProcessor runnable instance. Thread is "+Thread.currentThread().getName());
            ffmpegFrameExtractor.start(imageFilePath);
            startFFmpegCheckTimer();
            startImageProcessTimer();
        } catch (Exception e) {
            LOGGER.e(e, "monitoring exception ");
        }
    }

    private void startFFmpegCheckTimer(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(ffmpegFrameExtractor.isStopped()){
                    ffmpegFrameExtractor.start(imageFilePath);
                }
                oldMediaCleaner.cleanupMedia(context);
            }
        };
        ffmpegTimer.schedule(timerTask, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(30));
    }

    private void startImageProcessTimer(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try{
                    if(running.get()){
                        FrameEvent frameEvent = new FrameEvent(cameraConfig, imageFilePath, context);
                        imageProcessor.processImage(frameEvent);
                    }else{
                        this.cancel();
                    }
                }catch(Exception e){
                    LOGGER.e(e, "Error starting image processing");
                }
            }
        };
        imageProcessTimer.schedule(timerTask, TimeUnit.SECONDS.toMillis(10), 500);
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
}
