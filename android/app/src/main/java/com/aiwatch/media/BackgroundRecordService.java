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

public class BackgroundRecordService {

    private static final Logger LOGGER = new Logger();
    private Context context;
    private FFtask recordingTask;
    private CameraConfig cameraConfig;
    public BackgroundRecordService(Context context, CameraConfig cameraConfig){
        this.context = context;
        this.cameraConfig = cameraConfig;
    }

    public void startFFMpegRecording(){
        CustomFFmpeg ffmpeg = CustomFFmpeg.getInstance(context);
        File videoFolder = new File(context.getFilesDir(), AppConstants.UNCOMPRESSED_VIDEO_FOLDER);
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }
        String videoPath = videoFolder.getAbsolutePath();
        File imageFolder = new File(context.getFilesDir(), AppConstants.IMAGES_FOLDER);
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }
        long timeout = 10 * 1000000; //10 seconds
        String imagePath = imageFolder.getAbsolutePath();
        String recordCommand = " -codec copy -flags +global_header -f segment -strftime 1 -segment_time 30 -segment_format_options movflags=+faststart -reset_timestamps 1 " + videoPath + "/" + cameraConfig.getId() +"-%Y%m%d_%H:%M:%S.mp4 ";
        String frameExtractCommand =  " -vf select=eq(pict_type\\,PICT_TYPE_I) -update 1 -vsync vfr " + imagePath + "/camera" + cameraConfig.getId() +".png";
        String command = "-rtsp_transport tcp -i " + cameraConfig.getVideoUrl() + recordCommand + frameExtractCommand;
        String[] ffmpegCommand = command.split("\\s+");
        recordingTask = ffmpeg.execute(ffmpegCommand, new FFcommandExecuteResponseHandler() {
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
                //keep retrying
                //startFFMpegRecording(cameraId, videoUrl);
            }
        });
    }

    public void stopFFMpegRecording(){
        recordingTask.sendQuitSignal();
        long waitTime = 2*1000; //2 seconds
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                recordingTask.killRunningProcess();
            }
        }, waitTime);
    }

    public boolean isRecording(){
        return !recordingTask.isProcessCompleted();
    }

    public boolean killRecording(){
        return recordingTask.killRunningProcess();
    }
}
