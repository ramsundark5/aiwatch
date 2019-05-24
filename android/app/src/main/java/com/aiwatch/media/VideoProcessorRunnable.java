package com.aiwatch.media;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;
import android.util.TimingLogger;
import com.aiwatch.Logger;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.common.RTSPTimeOutOption;
import com.aiwatch.common.AppConstants;
import com.aiwatch.postprocess.DetectionResultProcessor;
import com.iceteck.silicompressorr.SiliCompressor;
import com.otaliastudios.transcoder.MediaTranscoder;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategies;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy;
import com.otaliastudios.transcoder.validator.WriteAlwaysValidator;


import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.bravobit.ffmpeg.CustomFFmpeg;
import nl.bravobit.ffmpeg.FFcommandExecuteResponseHandler;

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
        LOGGER.i("monitoring stop requested for camera "+cameraConfig.getId());
        running.set(false);
    }

    @Override
    public void run() {
        try {
            running.set(true);
            //monitor();
            startFFMpegRecording(cameraConfig.getId(), cameraConfig.getVideoUrl());
            //startFFMpegRecording(3, "rtsp://admin:230982@192.168.29.244:554/cam/realmonitor?channel=1&subtype=0");
            //startFFMpegRecording(4, "rtsp://admin:230982@192.168.29.244:554/cam/realmonitor?channel=2&subtype=0");
            //compressVideos(context.getFilesDir());
        } catch (Exception e) {
            LOGGER.e("compression exception " + e.getStackTrace());
            e.printStackTrace();
        }
        finally{
            //stopGrabber();
        }
    }

    private void compressVideos(File videoDir) throws URISyntaxException, IOException, ExecutionException, InterruptedException {
        FilenameFilter filenameFilter = new FilenameFilter(){
            public boolean accept(File dir, String name)
            {
                return ((name.endsWith(".mp4")));
            }
        };
        File compressedFolder = new File(videoDir, "compressed");
        if (!compressedFolder.exists()) {
            compressedFolder.mkdirs();
        }
        for(File rawFile : videoDir.listFiles(filenameFilter)){

            long startTime = System.nanoTime();
            File outputFile = new File(compressedFolder, rawFile.getName());
            DefaultVideoStrategy strategy2 = DefaultVideoStrategy.atMost(360, 480).
                    frameRate(15).
                    iFrameInterval(10F).
                    build();
            LOGGER.d("starting compression");
            Future compressFuture = MediaTranscoder.into(outputFile.getAbsolutePath())
                    .setValidator(new WriteAlwaysValidator())
                    .setDataSource(rawFile.getAbsolutePath())
                    .setVideoOutputStrategy(strategy2)
                    .setListener(new MediaTranscoder.Listener() {
                        public void onTranscodeProgress(double progress) {}
                        public void onTranscodeCompleted(int successCode) {
                            LOGGER.d("compression completed");
                        }
                        public void onTranscodeCanceled() {}
                        public void onTranscodeFailed(Throwable exception) {
                            LOGGER.e("error "+exception.getMessage());
                        }
                    }).transcode();
            compressFuture.get();

            /*String filePath = SiliCompressor.with(context).compressVideo(rawFile.getAbsolutePath(), compressedFolder.getAbsolutePath());
            File compressedFile = new File(filePath);
            if (compressedFile.exists()) {
                compressedFile.renameTo(outputFile);
            }*/
            long endTime = System.nanoTime();
            long time_ns = endTime - startTime;
            long time_s = TimeUnit.NANOSECONDS.toSeconds(time_ns);
            LOGGER.d("time taken "+time_s);
            LOGGER.d("compressed file created at path "+outputFile.getAbsolutePath());
        }
    }

    private void compressVideo2(File videoDir){
        FilenameFilter filenameFilter = new FilenameFilter(){
            public boolean accept(File dir, String name)
            {
                return ((name.endsWith(".mp4")));
            }
        };
        File compressedFolder = new File(videoDir, "compressed");
        if (!compressedFolder.exists()) {
            compressedFolder.mkdirs();
        }
    }

    private static void getOldFiles(){

    }

    private void startFFMpegRecording(long cameraId, String videoUrl){
        CustomFFmpeg ffmpeg = CustomFFmpeg.getInstance(context);
        File outputFile = new File(context.getFilesDir(), "cam");
        String videoPath = outputFile.getAbsolutePath();
        //-vf "select='eq(pict_type,PICT_TYPE_I)'" -vsync vfr thumb%04d.png
        //-f tee -map 0:v "[f=segment:segment_atclocktime=1:segment_time=3600:strftime=1]/videos/raw_video/video_%Y%m%d-%H%M%S.mp4|[f=mpegts]udp://127.0.0.1:1234/"
        String command = "-rtsp_transport tcp -i " + videoUrl +" -codec copy -flags +global_header -f segment -strftime 1 -segment_time 30 -segment_format_options movflags=+faststart -reset_timestamps 1 " + videoPath + cameraId +"-%Y%m%d_%H:%M:%S.mp4  -vf fps=1 " + videoPath + cameraId +"%d.png";
        String[] ffmpegCommand = command.split("\\s+");
        ffmpeg.execute(ffmpegCommand, new FFcommandExecuteResponseHandler() {
            @Override
            public void onStart() {
                LOGGER.d("ffmpeg recording started");
            }

            @Override
            public void onFinish() {
                LOGGER.d("ffmpeg recording completed");
            }

            @Override
            public void onSuccess(String message) {
                LOGGER.d("ffmpeg recording success");
            }

            @Override
            public void onProgress(String message) {
                LOGGER.d("ffmpeg recording in progress");
            }

            @Override
            public void onFailure(String message) {
                LOGGER.e("ffmpeg recording failed "+message);
            }
        });
        //String[] command = {"-rtsp_transport", "tcp", "-i", cameraConfig.getVideoUrl(), "-t", String.valueOf(recordingDuration), "-codec", "copy", filePath};
        //ffmpeg.execute()
    }

    private void monitor(){
        while (running.get()) {
            if (!pauseFrameGrabbing) {
                try {
                    Pair<FrameEvent, ObjectDetectionResult> resultPair = grabFrameAndProcess();
                    if (resultPair != null) {
                        pauseFrameGrabbing = detectionResultProcessor.processObjectDetectionResult(resultPair.first, resultPair.second);
                        if (pauseFrameGrabbing) {
                            stopGrabber();
                            long waitTimeInMins = cameraConfig.getWaitPeriodAfterDetection();
                            long waitTime = waitTimeInMins >= 1 ? waitTimeInMins * 60 * 1000 : AppConstants.WAIT_TIME_AFTER_DETECT;
                            waitTime = 20 * 1000; //20 secs
                            Thread.sleep(waitTime);
                            LOGGER.d("sleep is over and running flag is set to " + running.get());
                        }
                    }
                } catch (Exception e) {
                    //swallow exception to continue processing
                    LOGGER.e(e.getMessage());
                }
            }
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
            LOGGER.i("paused frame grabbing and running flag set to "+running.get());
        } catch (Exception e) {
            LOGGER.e(e.getMessage());
        }finally{
            grabber = null;
        }
    }
}
