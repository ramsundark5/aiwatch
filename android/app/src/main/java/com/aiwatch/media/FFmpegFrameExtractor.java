package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.aiwatch.common.FileUtil;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.postprocess.NotificationManager;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.util.AsyncSingleFFmpegExecuteTask;
import com.arthenica.mobileffmpeg.util.SingleExecuteCallback;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class FFmpegFrameExtractor {

    private static final Logger LOGGER = new Logger();
    private Context context;
    private CameraConfig cameraConfig;
    private boolean isfftaskCompleted = false;

    public FFmpegFrameExtractor(Context context, CameraConfig cameraConfig){
        this.context = context;
        this.cameraConfig = cameraConfig;
    }

    public void start(String imageFilePath) {
        if(!cameraConfig.isMonitoringEnabled() && !cameraConfig.isCvrEnabled() && !cameraConfig.isLiveHLSViewEnabled()){
            LOGGER.d("all monitoring flags are turned off. Skipping monitorinng for camera " + cameraConfig.getId());
            return;
        }
        try{
            isfftaskCompleted = false;
            String videoUrl = cameraConfig.getVideoUrlWithAuth();
            File imageFile = new File(imageFilePath);

            //this is important. ffmpeg runs in separate process and do not have permission on files unless it creates it
            if(imageFile.exists()){
                imageFile.delete();
            }
            File videoFolder = FileUtil.getApplicationDirectory(context, AppConstants.TEMP_VIDEO_FOLDER);
            String videoPath = videoFolder.getAbsolutePath();

            File cvrFolder = FileUtil.getBaseDirectory(context, AppConstants.CVR_VIDEO_FOLDER);
            String cvrPath = cvrFolder.getAbsolutePath();

            String cvrCommand = "";
            if(cameraConfig.isCvrEnabled()){
                cvrCommand = getRecordCommand(cvrPath, AppConstants.CVR_RECORDING_DURATION);
            }

            String preBufferRecordCommand = "";

            String frameExtractCommand = "";
            if(cameraConfig.isMonitoringEnabled()){
                //frameExtractCommand = " -vf select=eq(pict_type\\,PICT_TYPE_I),scale=300:300 -updatefirst 1 -vsync vfr " + imageFile.getAbsolutePath();
                frameExtractCommand =  " -vf select=eq(pict_type\\,PICT_TYPE_I),scale=300:300 -update 1 -vsync vfr " + imageFile.getAbsolutePath();
                preBufferRecordCommand = getRecordCommand(videoPath, AppConstants.PRE_RECORDING_BUFFER);
            }

            String liveViewCommand = "";
            String hlsIndexFileName = "";
            if(cameraConfig.isLiveHLSViewEnabled()){
                String hlsSegmentFileName = videoPath + "/" + cameraConfig.getId() +"-%d.ts ";
                //hlsIndexFileName = videoPath + "/camera" + cameraConfig.getId() + ".m3u8 ";
                hlsIndexFileName = videoPath + "/camera" + cameraConfig.getId() + System.currentTimeMillis() +".m3u8";
                liveViewCommand = " -f hls -codec copy " +
                        " -hls_flags delete_segments+append_list " +
                        " -hls_time 2 " +
                        " -hls_segment_filename " + hlsSegmentFileName +
                        hlsIndexFileName;
            }

            String lowLatencyPrefix = "";
            String rtspPrefix = "-rtsp_transport tcp ";
            if(videoUrl != null && !videoUrl.startsWith("rtsp")){
                rtspPrefix = "";
                //lowLatencyPrefix = "-fflags nobuffer -flags low_delay -probesize 32 -analyzeduration 0 ";
            }
            String command = lowLatencyPrefix + rtspPrefix + "-i " + videoUrl + frameExtractCommand + preBufferRecordCommand + cvrCommand + liveViewCommand;
            //String[] ffmpegCommand = command.split("\\s+");

            LOGGER.i("ffmpeg extraction starting. Thread is "+ Thread.currentThread().getName());
            notifyAndUpdateCameraStatus(false, hlsIndexFileName);

            final AsyncSingleFFmpegExecuteTask asyncCommandTask = new AsyncSingleFFmpegExecuteTask(command, new SingleExecuteCallback() {
                @Override
                public void apply(int returnCode, String executeOutput) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        LOGGER.i("ffmpeg extraction completed for camera "+cameraConfig.getId());
                    } else if (returnCode == RETURN_CODE_CANCEL) {
                        LOGGER.i("Command execution cancelled by user.");
                    } else {
                        LOGGER.e("ffmpeg extraction failed with response " + executeOutput);
                    }
                    isfftaskCompleted = true;
                    notifyAndUpdateCameraStatus(true, null);
                }
            });
            asyncCommandTask.executeOnExecutor(Executors.newSingleThreadExecutor());

        }catch(Exception e){
            LOGGER.e("Error starting ffmpeg extraction "+e);
        }
    }

    public boolean isStopped(){
        LOGGER.i("is ffmpeg process stopped ? "+isfftaskCompleted);
        return isfftaskCompleted;
    }

    public void stop(){
        try{
           FFmpeg.cancel();
           LOGGER.i("ffmpeg cancelled from camera ? "+cameraConfig.getId());
        }catch(Exception e){
            LOGGER.e(e, "Error stopping ffmpeg");
        }
    }

    private String getRecordCommand(String outputPath, int recordingDuration){
        String videoSegmentPrefix = " -codec copy -flags +global_header -f segment -strftime 1 -segment_time " + recordingDuration + " -segment_format_options movflags=+faststart -reset_timestamps 1 ";
        String recordCommand =  videoSegmentPrefix + outputPath + "/" + cameraConfig.getId() +"-%Y%m%d_%H:%M:%S.mp4 ";
        return recordCommand;
    }

    public void notifyAndUpdateWithDelay(boolean disconnected, String rtspUrl, long delay){
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                notifyAndUpdateCameraStatus(false, rtspUrl);
            }}, delay, TimeUnit.SECONDS);
    }

    public void notifyAndUpdateCameraStatus(boolean disconnected, String rtspUrl){
        try{
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            CameraConfig previousCameraConfig = cameraConfigDao.getCamera(cameraConfig.getId());
            LOGGER.d("Current camera disconnected status "+previousCameraConfig.isDisconnected());
            LOGGER.d("Requested camera disconnected status "+disconnected);
            String status = disconnected ? "disconnected" : "connected";
            cameraConfig.setDisconnected(disconnected);
            CameraConfig updatedCameraConfig = cameraConfigDao.updateCameraStatus(cameraConfig.getId(), disconnected);
            LOGGER.d("camera monitoring enabled status "+updatedCameraConfig.isMonitoringEnabled());
            updatedCameraConfig = cameraConfigDao.updateRtspUrl(cameraConfig.getId(), rtspUrl);
            LOGGER.d("camera rtspUrl "+updatedCameraConfig.getRtspUrl());
            NotificationManager.sendUINotification(context, updatedCameraConfig);
            if(previousCameraConfig.isDisconnected() == disconnected){
                NotificationManager.sendStringNotification(context, "Camera "+ cameraConfig.getName() + " "+ status);
            }
        }catch (Exception e){
            LOGGER.e(e, "error notifying camera status ");
        }
    }
}
