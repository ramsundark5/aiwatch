package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.aiwatch.media.db.CameraConfig;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import nl.bravobit.ffmpeg.CustomFFmpeg;
import nl.bravobit.ffmpeg.FFcommandExecuteResponseHandler;
import nl.bravobit.ffmpeg.FFtask;

public class FFmpegFrameExtractor {

    private static final Logger LOGGER = new Logger();
    private Context context;
    private FFtask recordingTask;
    private CameraConfig cameraConfig;

    public FFmpegFrameExtractor(Context context, CameraConfig cameraConfig){
        this.context = context;
        this.cameraConfig = cameraConfig;
    }

    public void start() {
        long cameraId = cameraConfig.getId();
        String videoUrl = cameraConfig.getVideoUrlWithAuth();
        CustomFFmpeg ffmpeg = CustomFFmpeg.getInstance(context);
        boolean isffmpegSupported = ffmpeg.isSupported();
        LOGGER.i("ffmpeg supported "+isffmpegSupported);
        File imageFolder = new File(context.getFilesDir(), AppConstants.IMAGES_FOLDER);
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }
        //long timeout = 10 * 1000000; //10 seconds
        String imageFolderPath = imageFolder.getAbsolutePath();
        File imageFile = new File(imageFolderPath, "/camera" + cameraId + ".png");
        if(imageFile.exists()){
            //this is important. ffmpeg runs in separate process and do not have permission on files unless it creates it
            imageFile.delete();
        }
        //"select='eq(pict_type,PICT_TYPE_I)'"
        //String recordCommand = " -codec copy -flags +global_header -f segment -strftime 1 -segment_time 30 -segment_format_options movflags=+faststart -reset_timestamps 1 " + videoPath + "/" + cameraId +"-%Y%m%d_%H:%M:%S.mp4 ";
        String frameExtractCommand =  " -vf select=eq(pict_type\\,PICT_TYPE_I),scale=300:300 -update 1 -vsync vfr " + imageFile.getAbsolutePath();
        String command = "-rtsp_transport tcp -i " + videoUrl + frameExtractCommand;
        String[] ffmpegCommand = command.split("\\s+");
        recordingTask = ffmpeg.execute(ffmpegCommand, new FFcommandExecuteResponseHandler() {
            @Override
            public void onStart() {
                LOGGER.d("ffmpeg recording started. Thread is "+ Thread.currentThread().getName());
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
                LOGGER.d("ffmpeg recording in progress. Thread is "+ Thread.currentThread().getName()+ " camera is "+cameraConfig.getId());
            }

            @Override
            public void onFailure(String message) {
                LOGGER.e("ffmpeg recording failed " + message);
                //keep retrying
                //startFFMpegRecording(cameraId, videoUrl);
            }
        });
    }

    public void stop(){
        recordingTask.sendQuitSignal();
        long waitTime = 2 * 1000; //2 seconds
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                recordingTask.killRunningProcess();
            }
        }, waitTime);
    }

    public boolean isRunning(){
        return !recordingTask.isProcessCompleted();
    }

    public boolean kill(){
        return recordingTask.killRunningProcess();
    }
}
