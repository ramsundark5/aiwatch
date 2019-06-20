package com.aiwatch.media;

import android.content.Context;
import android.os.FileObserver;

import com.aiwatch.Logger;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.common.AppConstants;
import com.aiwatch.postprocess.DetectionResultProcessor;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonitoringRunnable implements Runnable {

    private static final Logger LOGGER = new Logger();
    private AtomicBoolean running = new AtomicBoolean(false);
    private CameraConfig cameraConfig;
    private DetectionResultProcessor detectionResultProcessor;
    private ImageProcessor imageProcessor;
    private Context context;
    private FFmpegFrameExtractor fFmpegFrameExtractor;
    public static FileObserver observer;

    public MonitoringRunnable(CameraConfig cameraConfig, Context context) {
        try {
            this.cameraConfig = cameraConfig;
            this.context = context;
            this.detectionResultProcessor = new DetectionResultProcessor();
            this.imageProcessor = new ImageProcessor(context.getAssets());
            fFmpegFrameExtractor = new FFmpegFrameExtractor(context, cameraConfig);
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
    }

    public void stop() {
        LOGGER.i("monitoring stop requested for camera "+cameraConfig.getId());
        fFmpegFrameExtractor.stop();
        observer.stopWatching();
    }

    @Override
    public void run() {
        try {
            LOGGER.i("Creating new VideoProcessor runnable instance. Thread is "+Thread.currentThread().getName());
            fFmpegFrameExtractor.start();
            startWatching();
        } catch (Exception e) {
            LOGGER.e(e, "monitoring exception ");
        }
    }

    private void startWatching() {
        File imageFolder = new File(context.getFilesDir(), AppConstants.IMAGES_FOLDER);
        String imageFolderPath = imageFolder.getAbsolutePath();
        // set up a file observer to watch this directory
        observer = new FileObserver(imageFolderPath, FileObserver.MODIFY) {
            @Override
            public void onEvent(int event, final String file) {
                processImage(file);
            }
        };
        observer.startWatching();
    }

    private void processImage(final String file){
        try{
            ObjectDetectionResult objectDetectionResult = imageProcessor.processImage(file);
            if(objectDetectionResult != null){
                LOGGER.d("detected "+objectDetectionResult.getName());
                FrameEvent frameEvent = new FrameEvent(null, cameraConfig, context);
                detectionResultProcessor.processObjectDetectionResult(frameEvent, objectDetectionResult);
            }
        } catch (Exception e) {
            LOGGER.e(e, "Image process exception ");
        }

    }
}
