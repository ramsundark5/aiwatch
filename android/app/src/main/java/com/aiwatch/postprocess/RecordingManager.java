package com.aiwatch.postprocess;

import com.aiwatch.cloud.gdrive.GDriveServiceHelper;
import com.aiwatch.cloud.gdrive.GdriveManager;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.aiwatch.Logger;
import com.aiwatch.ai.Events;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.media.FrameEvent;
import com.aiwatch.media.db.CameraConfig;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS;

public class RecordingManager {

    private static final Logger LOGGER = new Logger();
    public static final String DEFAULT_EXTENSION = ".mp4";

    public static boolean shouldStartRecording(ObjectDetectionResult objectDetectionResult, CameraConfig cameraConfig){
        //conditionally notify based on camera config
        boolean shouldRecord = false;
        if(Events.PERSON_DETECTED_EVENT.getName().equals(objectDetectionResult.getName()) && cameraConfig.isRecordPersonDetect()){
            shouldRecord = true;
        }else if(Events.VEHICLE_DETECTED_EVENT.getName().equals(objectDetectionResult.getName()) && cameraConfig.isRecordVehicleDetect()){
            shouldRecord = true;
        }else if(Events.ANIMAL_DETECTED_EVENT.getName().equals(objectDetectionResult.getName()) && cameraConfig.isRecordAnimalDetect()){
            shouldRecord = true;
        }
        return shouldRecord;
    }

    public static String recordVideo(FrameEvent frameEvent){
        String videoPath = recordToLocal(frameEvent);
        recordToGdrive(frameEvent, videoPath);
        return videoPath;
    }

    public static String getFilePathToRecord(FrameEvent frameEvent, String extension){
        if(extension == null){
            extension = DEFAULT_EXTENSION;
        }
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmm");
        String currentTime = dateFormat.format(System.currentTimeMillis());
        String fileName = frameEvent.getCameraConfig().getId() + currentTime + extension;
        File outputFile = new File(frameEvent.getContext().getFilesDir(), fileName);
        return outputFile.getAbsolutePath();
    }

    private static String recordToLocal(FrameEvent frameEvent){
        String filePath = getFilePathToRecord(frameEvent, DEFAULT_EXTENSION);
        CameraConfig cameraConfig = frameEvent.getCameraConfig();
        int recordingDuration = cameraConfig.getRecordingDuration();
        if(recordingDuration <= 1){
            recordingDuration = 15;
        }
        String videoUrl = cameraConfig.getVideoUrl();
        String ffmpegRecordCommand = "-rtsp_transport tcp -i " + videoUrl + " -t "+ recordingDuration +" -codec copy "+ filePath;
        FFmpeg.execute(ffmpegRecordCommand);
        int rc = FFmpeg.getLastReturnCode();
        String output = FFmpeg.getLastCommandOutput();
        LOGGER.d("recording completed with status "+ rc);
        if (rc == RETURN_CODE_SUCCESS) {
            LOGGER.i("Video recording completed successfully.");
            return filePath;
        } else if (rc == RETURN_CODE_CANCEL) {
            LOGGER.i("Video recording cancelled by user.");
        } else {
            LOGGER.e(String.format("Command execution failed with rc=%d and output=%s.", rc, output));
        }
        return null;
    }

    private static String recordToGdrive(FrameEvent frameEvent, String videoPath){
        try{
            DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmm");
            String currentTime = dateFormat.format(System.currentTimeMillis());
            String fileName = frameEvent.getCameraConfig().getId() + currentTime + DEFAULT_EXTENSION;
            GDriveServiceHelper gDriveServiceHelper = GdriveManager.getGDriveServiceHelper(frameEvent.getContext());
            DateFormat monthDateFormat = new SimpleDateFormat("yyyy-MM");
            String currentMonth = monthDateFormat.format(System.currentTimeMillis());
            String appFolderId = gDriveServiceHelper.createFolder(GDriveServiceHelper.APP_FOLDER_NAME, null, true);
            String monthFolderId = gDriveServiceHelper.createFolder(currentMonth, appFolderId , false);
            com.google.api.services.drive.model.File gdriveFile = gDriveServiceHelper.createFile(fileName, monthFolderId);
            com.google.api.services.drive.model.File uploadedFileMetadata = gDriveServiceHelper.uploadFile(gdriveFile, videoPath);
            return uploadedFileMetadata.getId();
        }catch(Exception e){
            LOGGER.e("error recording to gdrive "+e);
            NotificationManager.sendStringNotification(frameEvent, "Cannot upload to Google Drive. Reconnect your account from settings screen.");
        }
        return null;
    }
}
