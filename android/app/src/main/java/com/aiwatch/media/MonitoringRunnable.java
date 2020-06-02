package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.common.FileUtil;
import com.aiwatch.common.SharedPreferenceUtil;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.common.AppConstants;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MonitoringRunnable implements Runnable {

    private static final Logger LOGGER = new Logger();
    private CameraConfig cameraConfig;
    private Context context;
    private FFmpegFrameExtractor ffmpegFrameExtractor;
    private ImageProcessor imageProcessor;
    private String imageFilePath;
    private OldMediaCleaner oldMediaCleaner;
    private Timer ffmpegTimer;
    private Timer imageProcessTimer;

    private static final String FFMPEG_CHECK_TIMER_NAME = "ffmpegCheckTimer";
    private static final String IMAGE_PROCESS_CHECK_TIMER_NAME = "imageProcessTimer";
    public MonitoringRunnable(CameraConfig cameraConfig, Context context) {
        try {
            this.cameraConfig = cameraConfig;
            this.context = context;
            this.ffmpegFrameExtractor = new FFmpegFrameExtractor(context, cameraConfig);
            this.imageFilePath = getImageFilePath();
            this.imageProcessor = new ImageProcessor(context);
            this.oldMediaCleaner = new OldMediaCleaner();
            this.ffmpegTimer = new Timer(FFMPEG_CHECK_TIMER_NAME);
            this.imageProcessTimer = new Timer(IMAGE_PROCESS_CHECK_TIMER_NAME);
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
    }

    public void stop() {
        SharedPreferenceUtil.setCameraMonitorStatus(context, cameraConfig.getId(), false);
        LOGGER.i("monitoring stop requested for camera "+cameraConfig.getId());
        ffmpegFrameExtractor.stop();
        ffmpegTimer.cancel();
        ffmpegTimer = null;
        imageProcessTimer.cancel();
        imageProcessTimer = null;
    }

    @Override
    public void run() {
        try {
            SharedPreferenceUtil.setCameraMonitorStatus(context, cameraConfig.getId(), true);
            LOGGER.i("Creating new VideoProcessor runnable instance. Thread is "+Thread.currentThread().getName());
            ffmpegFrameExtractor.start(imageFilePath);
            startFFmpegCheckTimer();
            startImageProcessTimer();
        } catch (Exception e) {
            LOGGER.e(e, "monitoring exception ");
        }
    }

    public boolean isFFmmpegStopped(){
        return ffmpegFrameExtractor.isStopped();
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
        if(ffmpegTimer == null){
            ffmpegTimer = new Timer(FFMPEG_CHECK_TIMER_NAME);
        }
        ffmpegTimer.schedule(timerTask, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(30));
    }

    private void startImageProcessTimer(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try{
                    if(SharedPreferenceUtil.isCameraMonitorRunning(context, cameraConfig.getId())){
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
        if(imageProcessTimer == null){
            imageProcessTimer = new Timer(IMAGE_PROCESS_CHECK_TIMER_NAME);
        }
        imageProcessTimer.schedule(timerTask, TimeUnit.SECONDS.toMillis(10), 500);
    }

    private String getImageFilePath(){
        File imageFolder = FileUtil.getApplicationDirectory(context, AppConstants.TEMP_IMAGES_FOLDER);
        String imageFolderPath = imageFolder.getAbsolutePath();
        File imageFile = new File(imageFolderPath, "/" + cameraConfig.getId() + "-camera.png");
        return imageFile.getAbsolutePath();
    }
}
