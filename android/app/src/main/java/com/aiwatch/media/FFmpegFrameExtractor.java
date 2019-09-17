package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.postprocess.NotificationManager;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import nl.bravobit.ffmpeg.CustomFFcommandExecuteAsyncTask;
import nl.bravobit.ffmpeg.CustomFFmpeg;
import nl.bravobit.ffmpeg.FFcommandExecuteResponseHandler;

public class FFmpegFrameExtractor {

    private static final Logger LOGGER = new Logger();
    private Context context;
    private CustomFFcommandExecuteAsyncTask ffTask;
    private CameraConfig cameraConfig;

    public FFmpegFrameExtractor(Context context, CameraConfig cameraConfig){
        this.context = context;
        this.cameraConfig = cameraConfig;
    }

    public void start(String imageFilePath) {
        if(ffTask != null && !ffTask.isProcessCompleted()){
            LOGGER.d("Tried to start process when its already running. Skipping the restart for camera "+cameraConfig.getId());
            return;
        }
        if(!cameraConfig.isMonitoringEnabled()){
            LOGGER.d("all monitoring flags are turned off. Skipping monitorinng for camera " + cameraConfig.getId());
            return;
        }
        String videoUrl = cameraConfig.getVideoUrlWithAuth();
        CustomFFmpeg ffmpeg = CustomFFmpeg.getInstance(context);
        boolean isffmpegSupported = ffmpeg.isSupported();
        LOGGER.i("ffmpeg supported "+isffmpegSupported);
        File imageFile = new File(imageFilePath);

        //this is important. ffmpeg runs in separate process and do not have permission on files unless it creates it
        if(imageFile.exists()){
            imageFile.delete();
        }
        File videoFolder = new File(context.getFilesDir(), AppConstants.UNCOMPRESSED_VIDEO_FOLDER);
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }
        String videoPath = videoFolder.getAbsolutePath();

        File cvrFolder = new File(context.getFilesDir(), AppConstants.CVR_VIDEO_FOLDER);
        if (!cvrFolder.exists()) {
            cvrFolder.mkdirs();
        }
        String cvrPath = cvrFolder.getAbsolutePath();

        String recordCommand = getRecordCommand(videoPath, AppConstants.PRE_RECORDING_BUFFER);
        String cvrCommand = "";
        if(cameraConfig.isCvrEnabled()){
            cvrCommand = getRecordCommand(cvrPath, AppConstants.CVR_RECORDING_DURATION);
        }
        String frameExtractCommand = " -vf select=eq(pict_type\\,PICT_TYPE_I),scale=300:300 -updatefirst 1 -vsync vfr " + imageFile.getAbsolutePath();
        //String frameExtractCommand =  " -vf select=eq(pict_type\\,PICT_TYPE_I),scale=300:300 -update 1 -vsync vfr " + imageFile.getAbsolutePath();
        String rtspPrefix = "-rtsp_transport tcp ";
        if(videoUrl != null && !videoUrl.startsWith("rtsp")){
            rtspPrefix = "";
        }
        String command = rtspPrefix + "-i " + videoUrl + frameExtractCommand + recordCommand + cvrCommand;
        String[] ffmpegCommand = command.split("\\s+");
        ffmpeg.setTimeout(AppConstants.FFMPEG_COMMAND_TIMEOUT * 1000); //80 seconds
        ffTask = (CustomFFcommandExecuteAsyncTask) ffmpeg.execute(ffmpegCommand, new FFcommandExecuteResponseHandler() {
            @Override
            public void onStart() {
                LOGGER.d("ffmpeg extraction started. Thread is "+ Thread.currentThread().getName());
                notifyAndUpdateCameraStatus(false);
            }

            @Override
            public void onFinish() {
                LOGGER.d("ffmpeg extraction finished");
                notifyAndUpdateCameraStatus(true);
                ffTask.destroyProcess();
            }

            @Override
            public void onSuccess(String message) {
                LOGGER.d("ffmpeg extraction success");
            }

            @Override
            public void onProgress(String message) {
                LOGGER.v("ffmpeg extraction in progress. Thread is "+ Thread.currentThread().getName()+ " camera is "+cameraConfig.getId());
            }

            @Override
            public void onFailure(String message) {
                LOGGER.e("ffmpeg extraction failed " + message);
            }
        });
    }

    private String getRecordCommand(String outputPath, int recordingDuration){
        String videoSegmentPrefix = " -codec copy -flags +global_header -f segment -strftime 1 -segment_time " + recordingDuration + " -segment_format_options movflags=+faststart -reset_timestamps 1 ";
        String recordCommand =  videoSegmentPrefix + outputPath + "/" + cameraConfig.getId() +"-%Y%m%d_%H:%M:%S.mp4 ";
        return recordCommand;
    }

    public void stop(){
        try{
            if(ffTask != null){
                ffTask.sendQuitSignal();
                long waitTime = 2 * 1000; //2 seconds
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ffTask.killRunningProcess();
                    }
                }, waitTime);
            }
        }catch(Exception e){
            LOGGER.e(e, "Error stopping ffmpeg");
        }
    }

    public boolean isStopped(){
        if(ffTask == null){
            return false;
        }
        boolean isfftaskCompleted = ffTask.isProcessCompleted();
        LOGGER.d("is ffmpeg process stopped? "+isfftaskCompleted);
        return isfftaskCompleted;
    }

    public boolean kill(){
        return ffTask.killRunningProcess();
    }

    public void notifyAndUpdateCameraStatus(boolean disconnected){
        try{
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            CameraConfig previousCameraConfig = cameraConfigDao.getCamera(cameraConfig.getId());
            if(previousCameraConfig.isDisconnected() == disconnected){
                //nothing new to notify
                return;
            }
            String status = disconnected ? "disconnected" : "connected";
            cameraConfig.setDisconnected(disconnected);
            NotificationManager.sendStringNotification(context, "Camera "+ cameraConfig.getName() + " "+ status);
            NotificationManager.sendUINotification(context, cameraConfig);
            cameraConfigDao.updateCameraStatus(cameraConfig.getId(), disconnected);
        }catch (Exception e){
            LOGGER.e(e, "error notifying camera status ");
        }
    }
}
