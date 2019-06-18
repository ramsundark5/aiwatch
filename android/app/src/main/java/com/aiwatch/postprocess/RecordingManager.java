package com.aiwatch.postprocess;

import android.content.Context;

import com.aiwatch.cloud.gdrive.GDriveServiceHelper;
import com.aiwatch.cloud.gdrive.GdriveManager;
import com.aiwatch.Logger;
import com.aiwatch.ai.Events;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.media.FrameEvent;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.Settings;
import com.aiwatch.media.db.SettingsDao;
import com.google.common.net.MediaType;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import nl.bravobit.ffmpeg.CustomFFmpeg;
import nl.bravobit.ffmpeg.FFcommandExecuteResponseHandler;

public class RecordingManager {

    private static final Logger LOGGER = new Logger();
    public static final String DEFAULT_VIDEO_EXTENSION = ".mp4";
    public static final String DEFAULT_IMAGE_EXTENSION = ".png";

    public static boolean shouldStartRecording(ObjectDetectionResult objectDetectionResult, CameraConfig cameraConfig){
        //conditionally notify based on camera config
        boolean shouldRecord = false;
        if(Events.PERSON_DETECTED_EVENT.getName().equals(objectDetectionResult.getName()) && cameraConfig.isRecordPersonDetect()){
            shouldRecord = true;
        }else if(Events.VEHICLE_DETECTED_EVENT.getName().equals(objectDetectionResult.getName()) && cameraConfig.isRecordVehicleDetect()){
            shouldRecord = true;
        }else if(Events.ANIMAL_DETECTED_EVENT.getName().equals(objectDetectionResult.getName()) && cameraConfig.isRecordAnimalDetect()){
            shouldRecord = true;
        }else if(cameraConfig.isTestModeEnabled()){
            shouldRecord = true;
        }
        return shouldRecord;
    }

    public static String recordToLocal(FrameEvent frameEvent){
        try{
            CustomFFmpeg ffmpeg = CustomFFmpeg.getInstance(frameEvent.getContext());
            if (ffmpeg.isSupported()) {
                LOGGER.d("FFmpeg is supported");
            } else {
                LOGGER.e("FFmpeg is not supported");
            }
            String filePath = getFilePathToRecord(frameEvent, DEFAULT_VIDEO_EXTENSION);
            CameraConfig cameraConfig = frameEvent.getCameraConfig();
            int recordingDuration = cameraConfig.getRecordingDuration();
            if(recordingDuration <= 1){
                recordingDuration = 15;
            }
            String videoUrl = cameraConfig.getVideoUrlWithAuth();
            String recordCommand = "-rtsp_transport tcp -i " + videoUrl + " -t "+ recordingDuration + " -codec copy "+filePath;
            String[] ffmpegCommand = recordCommand.split("\\s+");
            String response = ffmpeg.executeSync(ffmpegCommand, new FFcommandExecuteResponseHandler() {
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
                    LOGGER.v("ffmpeg recording in progress. Thread is "+ Thread.currentThread().getName());
                }

                @Override
                public void onFailure(String message) {
                    LOGGER.e("ffmpeg recording failed " + message);
                }
            });
            LOGGER.d("record to local returned "+ response);
            return filePath;
        }catch (Exception e){
            LOGGER.e("Error recording video to local "+ e.getMessage());
        }
        return null;
    }

    public static String saveToGdrive(Context context, long cameraId, String inputFilePath, String mimeType, String extension){
        try{
            SettingsDao settingsDao = new SettingsDao();
            Settings settings = settingsDao.getSettings();
            if(!settings.isGoogleAccountConnected()){
                return null;
            }
            if(mimeType == null){
                mimeType = MediaType.MP4_VIDEO.toString();
            }
            if(extension == null){
                extension = DEFAULT_VIDEO_EXTENSION;
            }
            DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmm");
            String currentTime = dateFormat.format(System.currentTimeMillis());
            String fileName = cameraId + currentTime + extension;
            GDriveServiceHelper gDriveServiceHelper = GdriveManager.getGDriveServiceHelper(context);
            if(gDriveServiceHelper == null){
                return null;
            }
            DateFormat monthDateFormat = new SimpleDateFormat("yyyy-MM");
            String currentMonth = monthDateFormat.format(System.currentTimeMillis());
            String appFolderId = gDriveServiceHelper.createFolder(GDriveServiceHelper.APP_FOLDER_NAME, null, true);
            String monthFolderId = gDriveServiceHelper.createFolder(currentMonth, appFolderId , false);
            com.google.api.services.drive.model.File uploadedFileMetadata = gDriveServiceHelper.uploadFile(fileName, monthFolderId, inputFilePath, mimeType);
            return uploadedFileMetadata.getId();
        }catch(Exception e){
            LOGGER.e("error saving to gdrive "+e);
            NotificationManager.sendStringNotification(context, "Cannot upload to Google Drive. Reconnect your account from settings screen.");
        }
        return null;
    }

    public static String getFilePathToRecord(FrameEvent frameEvent, String extension){
        if(extension == null){
            extension = DEFAULT_VIDEO_EXTENSION;
        }
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmm");
        String currentTime = dateFormat.format(System.currentTimeMillis());
        String fileName = frameEvent.getCameraConfig().getId() + currentTime + extension;
        File outputFile = new File(frameEvent.getContext().getFilesDir(), fileName);
        return outputFile.getAbsolutePath();
    }
}
