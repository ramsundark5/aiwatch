package com.aiwatch.media;

import android.content.Context;
import android.util.Pair;
import android.util.TimingLogger;
import com.aiwatch.Logger;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.common.AppConstants;
import com.aiwatch.postprocess.DetectionResultProcessor;

import org.bytedeco.javacv.Frame;
import java.util.concurrent.atomic.AtomicBoolean;

public class MonitoringRunnable implements Runnable {

    private static final Logger LOGGER = new Logger();
    private AtomicBoolean running = new AtomicBoolean(false);
    private CameraConfig cameraConfig;
    private DetectionResultProcessor detectionResultProcessor;
    private VideoFrameExtractor videoFrameExtractor;
    private ImageProcessor imageProcessor;
    private Context context;
    private boolean pauseFrameGrabbing = false;
    private int framesGrabbed = 0;

    public MonitoringRunnable(CameraConfig cameraConfig, Context context) {
        try {
            this.cameraConfig = cameraConfig;
            this.context = context;
            this.detectionResultProcessor = new DetectionResultProcessor();
            this.imageProcessor = new ImageProcessor(context.getAssets());
            this.videoFrameExtractor = new VideoFrameExtractor(cameraConfig, context);
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
    }

    public void stop() {
        LOGGER.i("monitoring stop requested for camera "+cameraConfig.getId());
        running.set(false);
        videoFrameExtractor.stopGrabber();
    }

    @Override
    public void run() {
        try {
            LOGGER.i("Creating new VideoProcessor runnable instance. Thread is "+Thread.currentThread().getName());
            running.set(true);
            monitor();
        } catch (Exception e) {
            LOGGER.e(e, "monitoring exception ");
        }
        finally{
            videoFrameExtractor.stopGrabber();
            framesGrabbed = 0;
            LOGGER.i("stopping monitoring runnable for camera "+cameraConfig.getId());
        }
    }

    private void monitor(){
        while (running.get()) {
            if (!pauseFrameGrabbing) {
                try {
                    Pair<FrameEvent, ObjectDetectionResult> resultPair = grabFrameAndProcess();
                    if (resultPair != null) {
                        pauseFrameGrabbing = detectionResultProcessor.processObjectDetectionResult(resultPair.first, resultPair.second);
                        if (pauseFrameGrabbing) {
                            pauseFrameGrabbing();
                        }
                    }
                } catch (Exception e) {
                    //swallow exception to continue processing
                    LOGGER.e(e, e.getMessage());
                }
            }
        }
    }

    private Pair<FrameEvent, ObjectDetectionResult> grabFrameAndProcess() throws Exception {
        Frame frame;
        TimingLogger timings = new TimingLogger(LOGGER.DEFAULT_TAG, "Framegrabber performance");
        frame = videoFrameExtractor.grabFrame();
        timings.addSplit("Frame grab time");
        if (frame != null) {
            if(!frame.keyFrame){
                return null;
            }
            LOGGER.d("just grabbed a frame for camera "+cameraConfig.getId());
            framesGrabbed++;
            FrameEvent frameEvent = new FrameEvent(frame, cameraConfig, context);
            LOGGER.d("start processing next frame. Thread is "+ Thread.currentThread().getName());
            ObjectDetectionResult objectDetectionResult = imageProcessor.processImage(frameEvent);
            LOGGER.d("frames grabbed "+ framesGrabbed);
            timings.dumpToLog();
            Pair<FrameEvent, ObjectDetectionResult> resultPair = Pair.create(frameEvent, objectDetectionResult);
            return resultPair;
        } else { // when frame == null then connection has been lost
            LOGGER.i("no frame returned for camera "+cameraConfig.getId());
            LOGGER.i("reconnecting to camera..");
            videoFrameExtractor.initGrabber(cameraConfig);
        }
        return null;
    }

    private void pauseFrameGrabbing() throws InterruptedException {
        long waitTimeInMins = cameraConfig.getWaitPeriodAfterDetection();
        long waitTime = waitTimeInMins >= 1 ? waitTimeInMins * 60 * 1000 : AppConstants.WAIT_TIME_AFTER_DETECT;
        waitTime = 30 * 1000; //20 secs

        videoFrameExtractor.stopGrabber();
        LOGGER.i("paused frame grabbing. sleep time " + waitTime + ". Running flag set to "+running.get());
        LOGGER.i("paused frame grabbing and running flag set to "+running.get());
        framesGrabbed = 0;
        Thread.sleep(waitTime);
        pauseFrameGrabbing = false;
        LOGGER.d("sleep is over and running flag is set to " + running.get());
    }
}
