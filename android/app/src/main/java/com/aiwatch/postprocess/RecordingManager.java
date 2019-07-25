package com.aiwatch.postprocess;

import android.content.Context;

import com.aiwatch.cloud.gdrive.GDriveServiceHelper;
import com.aiwatch.cloud.gdrive.GdriveManager;
import com.aiwatch.Logger;
import com.aiwatch.ai.Events;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.common.AppConstants;
import com.aiwatch.media.FrameEvent;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.Settings;
import com.aiwatch.media.db.SettingsDao;
import com.google.common.net.MediaType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;
import nl.bravobit.ffmpeg.CustomFFmpeg;
import nl.bravobit.ffmpeg.CustomResponseHandler;

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
        File inputFileList = getInputFileList(frameEvent);
        try{
            CustomFFmpeg ffmpeg = CustomFFmpeg.getInstance(frameEvent.getContext());
            if (ffmpeg.isSupported()) {
                LOGGER.d("FFmpeg is supported");
            } else {
                LOGGER.e("FFmpeg is not supported");
            }
            String filePath = getFilePathToRecord(frameEvent, DEFAULT_VIDEO_EXTENSION);
            if(inputFileList == null){
                //nothing to process
                return null;
            }
            String inputFilePath = inputFileList.getAbsolutePath();
            String recordCommand = " -f concat -i " + inputFilePath + " -c copy " + filePath;
            String[] ffmpegCommand = recordCommand.split("\\s+");
            String response = ffmpeg.executeSync(ffmpegCommand, new CustomResponseHandler("video merging") );
            LOGGER.d("record to local returned "+ response);
            return filePath;
        }catch (Exception e){
            LOGGER.e("Error merging video "+ e.getMessage());
        }finally {
            if(inputFileList != null && inputFileList.exists()){
                inputFileList.delete();
            }
        }
        return null;
    }

    public static String saveToGdrive(Context context, long cameraId, String inputFilePath, String mimeType, String extension){
        try{
            SettingsDao settingsDao = new SettingsDao();
            Settings settings = settingsDao.getSettings();
            if(settings == null || !settings.isGoogleAccountConnected()){
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

    private static File getInputFileList(FrameEvent frameEvent){
        File inputFileList = null;
        try{
            File videoFolder = new File(frameEvent.getContext().getFilesDir(), AppConstants.UNCOMPRESSED_VIDEO_FOLDER);
            CameraConfig cameraConfig = frameEvent.getCameraConfig();

            File[] inputFiles = videoFolder.listFiles(file -> {
                int recordingDuration = cameraConfig.getRecordingDuration();
                if(recordingDuration <= 1){
                    recordingDuration = 15;
                }
                String cameraIdPrefix = cameraConfig.getId() + "-";
                long lastModified = file.lastModified();
                boolean isFileBelongToCamera = file.getName().startsWith(cameraIdPrefix);
                boolean isFileBelongToTimeRange = false;
                if(System.currentTimeMillis() - lastModified > (recordingDuration + AppConstants.PRE_RECORDING_BUFFER)){
                    isFileBelongToTimeRange = true;
                }
                return isFileBelongToCamera && isFileBelongToTimeRange;
            });
            inputFileList = File.createTempFile(UUID.randomUUID().toString(), ".txt");
            generateList(inputFiles, inputFileList);
        }catch(Exception e){
            LOGGER.e(e, "Error generating inout file list");
        }
        return inputFileList;
    }

    /**
     * Generate an ffmpeg file list
     * @param inputs Input files for ffmpeg
     * @return output File
     */
    private static File generateList(File[] inputs, File outputFile) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
            for (File input: inputs) {
                writer.write("file '" + input.getAbsolutePath() + "'\n");
                LOGGER.d( "Writing to list file: file '" + input + "'");
            }
            LOGGER.d( "Wrote list file to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.e(e, "Error generating list for merging");
            return null;
        }  finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException ex) {
                LOGGER.e(ex, "Error closing resource after video merging");
            }
        }
        return outputFile;
    }
}

