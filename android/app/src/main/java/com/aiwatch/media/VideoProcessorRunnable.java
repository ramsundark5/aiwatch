package com.aiwatch.media;

import android.content.Context;
import android.util.Pair;
import android.util.TimingLogger;
import com.aiwatch.Logger;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.common.RTSPTimeOutOption;
import com.aiwatch.common.AppConstants;
import com.aiwatch.postprocess.DetectionResultProcessor;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoProcessorRunnable implements Runnable {

    private static final Logger LOGGER = new Logger();
    private AtomicBoolean running = new AtomicBoolean(false);
    private CameraConfig cameraConfig;
    private FFmpegFrameGrabber grabber;
    private DetectionResultProcessor detectionResultProcessor;
    private ImageProcessor imageProcessor;
    private Context context;
    private boolean pauseFrameGrabbing = false;
    private int framesGrabbed = 0;

    public VideoProcessorRunnable(CameraConfig cameraConfig, Context context) {
        try {
            this.cameraConfig = cameraConfig;
            this.context = context;
            this.detectionResultProcessor = new DetectionResultProcessor();
            this.imageProcessor = new ImageProcessor(context.getAssets());
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
    }

    public void stop() {
        running.set(false);
    }

    @Override
    public void run() {
        try {
            running.set(true);
            while (running.get()) {
                if(!pauseFrameGrabbing){
                    try{
                        Pair<FrameEvent, ObjectDetectionResult> resultPair = grabFrameAndProcess();
                        if(resultPair != null){
                            pauseFrameGrabbing = detectionResultProcessor.processObjectDetectionResult(resultPair.first, resultPair.second);
                            if(pauseFrameGrabbing){
                                stopGrabber();
                                long waitTimeInMins = cameraConfig.getWaitPeriodAfterDetection();
                                long waitTime = waitTimeInMins >= 1 ? waitTimeInMins * 60 * 1000 : AppConstants.WAIT_TIME_AFTER_DETECT;
                                waitTime = 20 * 1000; //20 secs
                                Thread.sleep(waitTime);
                            }
                        }
                    }catch(Exception e){
                        //swallow exception to continue processing
                        LOGGER.e(e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }
        finally{
            stopGrabber();
        }
    }

    private Pair<FrameEvent, ObjectDetectionResult> grabFrameAndProcess() throws Exception {
        if (grabber == null) {
            initGrabber(cameraConfig); // connect
        }
        Frame frame;
        TimingLogger timings = new TimingLogger(LOGGER.DEFAULT_TAG, "Framegrabber performance");
        try{
            frame = grabber.grabImage();
        }catch(Exception e){
            LOGGER.e("Exception grabbing frame" + e.getMessage());
            return null;
        }
        LOGGER.d("just grabbed a frame for camera "+cameraConfig.getId());
        timings.addSplit("Frame grab time");
        if (frame != null) {
            if(!frame.keyFrame){
                return null;
            }
            framesGrabbed++;
            FrameEvent frameEvent = new FrameEvent(frame, cameraConfig, context);
            LOGGER.d("start processing next frame "+ Thread.currentThread().getName());
            ObjectDetectionResult objectDetectionResult = imageProcessor.processImage(frameEvent);
            LOGGER.d("frames grabbed "+ framesGrabbed);
            timings.dumpToLog();
            Pair<FrameEvent, ObjectDetectionResult> resultPair = Pair.create(frameEvent, objectDetectionResult);
            return resultPair;
        } else { // when frame == null then connection has been lost
            LOGGER.i("no frame returned for camera "+cameraConfig.getId());
            LOGGER.i("reconnecting to camera..");
            initGrabber(cameraConfig); // reconnect
        }
        return null;
    }

    private void initGrabber(CameraConfig cameraConfig) throws Exception {
        int TIMEOUT = 10; //10 secs
        grabber = new FFmpegFrameGrabber(cameraConfig.getVideoUrl()); // rtsp url
        //rtsp_transport flag is important. Otherwise grabbed image will be distorted
        grabber.setOption("rtsp_transport", "tcp");
        grabber.setVideoCodec(cameraConfig.getVideoCodec());
        grabber.setOption(
                RTSPTimeOutOption.STIMEOUT.getKey(),
                String.valueOf(TIMEOUT * 1000000)
        ); // In microseconds.
        grabber.setOption("hwaccel", "h264_videotoolbox");
        grabber.start();
        LOGGER.i("connected to camera "+cameraConfig.getId());
    }

    private void stopGrabber(){
        try {
            if(grabber != null){
                grabber.stop();
            }
            pauseFrameGrabbing = false;
            framesGrabbed = 0;
            LOGGER.i("paused frame grabbing");
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }finally{
            grabber = null;
        }
    }
}
