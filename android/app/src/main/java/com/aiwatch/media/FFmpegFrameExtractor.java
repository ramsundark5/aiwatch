package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.postprocess.NotificationManager;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import nl.bravobit.ffmpeg.CustomFFmpeg;
import nl.bravobit.ffmpeg.FFcommandExecuteResponseHandler;
import nl.bravobit.ffmpeg.FFtask;

public class FFmpegFrameExtractor {

    private static final Logger LOGGER = new Logger();
    private Context context;
    private FFtask ffTask;
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
            LOGGER.d("all monitoring flags are turned off. Skipping monitorinng for camera "+cameraConfig.getId());
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

        String frameExtractCommand =  " -vf select=eq(pict_type\\,PICT_TYPE_I),scale=300:300 -update 1 -vsync vfr " + imageFile.getAbsolutePath();
        String command = "-rtsp_transport tcp -i " + videoUrl + frameExtractCommand;
        String[] ffmpegCommand = command.split("\\s+");
        ffTask = ffmpeg.execute(ffmpegCommand, new FFcommandExecuteResponseHandler() {
            @Override
            public void onStart() {
                LOGGER.d("ffmpeg recording started. Thread is "+ Thread.currentThread().getName());
                notifyAndUpdateCameraStatus(false);
            }

            @Override
            public void onFinish() {
                notifyAndUpdateCameraStatus(true);
            }

            @Override
            public void onSuccess(String message) {
                LOGGER.d("ffmpeg recording success");
            }

            @Override
            public void onProgress(String message) {
                LOGGER.v("ffmpeg recording in progress. Thread is "+ Thread.currentThread().getName()+ " camera is "+cameraConfig.getId());
            }

            @Override
            public void onFailure(String message) {
                LOGGER.e("ffmpeg recording failed " + message);
            }
        });
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

    public boolean isRunning(){
        if(ffTask == null){
            return false;
        }
        return !ffTask.isProcessCompleted();
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
