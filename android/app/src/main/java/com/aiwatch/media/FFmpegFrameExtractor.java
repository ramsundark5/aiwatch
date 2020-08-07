package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.aiwatch.common.FileUtil;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.postprocess.NotificationManager;
import java.io.File;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class FFmpegFrameExtractor {

    private static final Logger LOGGER = new Logger();
    private Context context;
    private CameraConfig cameraConfig;
    private boolean isfftaskCompleted = false;
    private long executionId;

    public FFmpegFrameExtractor(Context context, CameraConfig cameraConfig){
        this.context = context;
        this.cameraConfig = cameraConfig;
    }

    public void start(String imageFilePath) {
        if(!cameraConfig.isMonitoringEnabled() && !cameraConfig.isCvrEnabled()){
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

            String lowLatencyPrefix = "";
            String rtspPrefix = "-rtsp_transport tcp ";
            if(videoUrl != null && !videoUrl.startsWith("rtsp")){
                rtspPrefix = "";
                //lowLatencyPrefix = "-fflags nobuffer -flags low_delay -probesize 32 -analyzeduration 0 ";
            }
            String command = lowLatencyPrefix + rtspPrefix + "-i " + videoUrl + frameExtractCommand + preBufferRecordCommand + cvrCommand;

            //String[] ffmpegCommand = command.split("\\s+");
            notifyAndUpdateCameraStatus(false);

            executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {

                @Override
                public void apply(final long executionId, final int returnCode) {
                    if (returnCode == RETURN_CODE_SUCCESS) {
                        LOGGER.i("ffmpeg extraction completed for camera "+cameraConfig.getId());
                    } else if (returnCode == RETURN_CODE_CANCEL) {
                        LOGGER.i("Command execution cancelled by user for camera "+cameraConfig.getId());
                    } else {
                        LOGGER.e("ffmpeg extraction failed for camera " + cameraConfig.getId() + "with response " + Config.getLastCommandOutput());
                    }
                    isfftaskCompleted = true;
                    notifyAndUpdateCameraStatus(true);
                }
            });

        }catch(Exception e){
            LOGGER.e("Error starting ffmpeg extraction "+e);
        }
    }

    public void stop(){
        try{
            FFmpeg.cancel(executionId);
        }catch(Exception e){
            LOGGER.e(e, "Error stopping ffmpeg");
        }
    }

    public boolean isStopped(){
        LOGGER.d("is ffmpeg process stopped? "+isfftaskCompleted);
        return isfftaskCompleted;
    }

    private String getRecordCommand(String outputPath, int recordingDuration){
        String videoSegmentPrefix = " -codec copy -f segment -strftime 1 -segment_time " + recordingDuration + " -segment_format mp4 ";
        String recordCommand =  videoSegmentPrefix + outputPath + "/" + cameraConfig.getId() +"-%Y%m%d_%H:%M:%S.mp4 ";
        return recordCommand;
    }
//-map 0 -f segment -segment_time 60 -reset_timestamps 1 -segment_format avi "/vidcam2cont/ffmpeg_capture-%03d.avi
    public void notifyAndUpdateCameraStatus(boolean disconnected){
        try{
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            CameraConfig previousCameraConfig = cameraConfigDao.getCamera(cameraConfig.getId());
            LOGGER.d("Current camera disconnected status "+previousCameraConfig.isDisconnected());
            LOGGER.d("Requested camera disconnected status "+disconnected);
            String status = disconnected ? "disconnected" : "connected";
            cameraConfig.setDisconnected(disconnected);
            CameraConfig updatedCameraConfig = cameraConfigDao.updateCameraStatus(cameraConfig.getId(), disconnected);
            LOGGER.d("camera monitoring enabled status "+updatedCameraConfig.isMonitoringEnabled());
            NotificationManager.sendUINotification(context, updatedCameraConfig);
            if(previousCameraConfig.isDisconnected() == disconnected){
                NotificationManager.sendStringNotification(context, "Camera "+ cameraConfig.getName() + " "+ status);
            }
        }catch (Exception e){
            LOGGER.e(e, "error notifying camera status ");
        }
    }
}
