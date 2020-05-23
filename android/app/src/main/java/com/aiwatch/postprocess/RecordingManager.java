package com.aiwatch.postprocess;

import android.content.Context;
import com.aiwatch.cloud.gdrive.GDriveServiceHelper;
import com.aiwatch.cloud.gdrive.GdriveManager;
import com.aiwatch.Logger;
import com.aiwatch.ai.Events;
import com.aiwatch.ai.ObjectDetectionResult;
import com.aiwatch.common.AppConstants;
import com.aiwatch.common.FileUtil;
import com.aiwatch.media.FrameEvent;
import com.aiwatch.models.AlarmEvent;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.models.Settings;
import com.aiwatch.media.db.SettingsDao;
import com.google.common.net.MediaType;

import org.joda.time.DateTime;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

    public static String recordToLocal(FrameEvent frameEvent, AlarmEvent alarmEvent){
        String filePath = null;
        try{
            File[] inputFileList = getInputFileList(frameEvent, alarmEvent);
            File baseDirectory = FileUtil.getBaseDirectory(frameEvent.getContext(), AppConstants.EVENT_VIDEO_FOLDER);
            filePath = getFilePathToRecord(frameEvent.getCameraConfig().getId(), baseDirectory, DEFAULT_VIDEO_EXTENSION);
            if(inputFileList == null || inputFileList.length < 1){
                //nothing to process
                return null;
            }
            List<String> inputPathsList = new ArrayList<>();
            for(File inputFile : inputFileList){
                inputPathsList.add(inputFile.getAbsolutePath());
            }
            String[] inputVideoPaths = inputPathsList.toArray(new String[0]);
            //VideoMerger videoMerger = new VideoMerger();
            //videoMerger.appendVideo(inputVideoPaths, filePath);
            FFMpegVideoMerger ffMpegVideoMerger = new FFMpegVideoMerger();
            ffMpegVideoMerger.mergeVideos(inputVideoPaths, filePath);
            File outpuFile = new File(filePath);
            if(outpuFile == null || !outpuFile.exists() || outpuFile.length() < 1){
                filePath = null;
            }
        }catch (Exception e){
            LOGGER.e(e, "Error merging video "+ e.getMessage());
        }
        return filePath;
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
            LOGGER.e(e, "error saving to gdrive ");
            NotificationManager.sendStringNotification(context, "Cannot upload to Google Drive. Reconnect your account from settings screen.");
        }
        return null;
    }

    public static String getFilePathToRecord(long cameraId, File baseDirectory, String extension){
        if(extension == null){
            extension = DEFAULT_VIDEO_EXTENSION;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH:mm:ss");
        String currentTime = dateFormat.format(System.currentTimeMillis());
        String fileName = cameraId + "-"+ currentTime + extension;
        File outputFile = new File(baseDirectory, fileName);
        return outputFile.getAbsolutePath();
    }

    private static File[] getInputFileList(FrameEvent frameEvent, AlarmEvent alarmEvent){
        try{
            File videoFolder = FileUtil.getApplicationDirectory(frameEvent.getContext(), AppConstants.TEMP_VIDEO_FOLDER);
            CameraConfig cameraConfig = frameEvent.getCameraConfig();
            DateTime currentTime = new DateTime();
            DateTime eventTime = new DateTime(alarmEvent.getDate());
            DateTime startTime = eventTime.minusSeconds(AppConstants.PRE_RECORDING_BUFFER + 2);
            DateTime endTime = currentTime.minusSeconds(2);
            LOGGER.d("current Time " + currentTime.toString("HH:mm:ss"));
            LOGGER.d("alarmevent time " + eventTime.toString("HH:mm:ss"));
            LOGGER.d("recording startTime " + startTime.toString("HH:mm:ss"));
            File[] inputFiles = videoFolder.listFiles(file -> {
                String cameraIdPrefix = cameraConfig.getId() + "-";
                boolean isFileBelongToCamera = file.getName().startsWith(cameraIdPrefix);
                boolean isFileBelongToTimeRange = false;
                DateTime fileLastModifiedTime = new DateTime(file.lastModified());
                if( fileLastModifiedTime.isAfter(startTime) && fileLastModifiedTime.isBefore(endTime)){
                    LOGGER.d("file to be merged lastmodified " + fileLastModifiedTime.toString("HH:mm:ss"));
                    isFileBelongToTimeRange = true;
                }
                return isFileBelongToCamera && isFileBelongToTimeRange;
            });
            return inputFiles;
        }catch(Exception e){
            LOGGER.e(e, "Error generating inout file list");
        }
        return null;
    }
}

