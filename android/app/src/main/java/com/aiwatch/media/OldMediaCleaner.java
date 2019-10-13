package com.aiwatch.media;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.aiwatch.common.FileUtil;
import com.aiwatch.media.db.SettingsDao;
import com.aiwatch.models.Settings;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class OldMediaCleaner {
    private static final Logger LOGGER = new Logger();

    public void cleanupMedia(Context context){
        LOGGER.d("Starting old files cleanup");
        cleanupTempVideos(context);

        //cleanup old media if low on storage space
        if(isLowOnStorageSpace(context)){
            cleanupOldFiles(context, AppConstants.CVR_VIDEO_FOLDER);
            cleanupOldFiles(context, AppConstants.EVENT_VIDEO_FOLDER);
            cleanupOldFiles(context, AppConstants.EVENT_IMAGES_FOLDER);
        }
    }

    private void cleanupTempVideos(Context context){
        try{
            File videoFolder = FileUtil.getApplicationDirectory(context, AppConstants.TEMP_VIDEO_FOLDER);
            File[] filesToBeDeleted = videoFolder.listFiles(file -> {
                long lastModified = file.lastModified();
                boolean isOldFile = false;
                if(System.currentTimeMillis() - lastModified > TimeUnit.MINUTES.toMillis(2)){
                    isOldFile = true;
                }
                return isOldFile;
            });

            for(File file: filesToBeDeleted){
                file.delete();
                LOGGER.d("Deleted old file "+file.getAbsolutePath());
            }
            LOGGER.d("Completed old files cleanup");
        }catch (Exception e){
            LOGGER.e(e, "Error deleting old files");
        }
    }

    private void cleanupOldFiles(Context context, String folderName){
        try{
            File filesDirectory = FileUtil.getBaseDirectory(context, folderName);
            File[] files = filesDirectory.listFiles();

            if(files == null || files.length < 1){
                return;
            }

            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.compare(f1.lastModified(), f2.lastModified());
                }
            });

            File oldestFile = files[0];
            String oldestFilePath = oldestFile.getAbsolutePath();
            oldestFile.delete();
            LOGGER.d("deleted oldest file with name "+oldestFilePath);
        }catch (Exception e){
            LOGGER.e(e, e.getMessage());
        }
    }

    private boolean isLowOnStorageSpace(Context context){
        double MIN_FREE_STORAGE = 2.5;
        boolean lowOnStorage = false;
        try{
            long freeBytes;
            freeBytes = new File(context.getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
            if(isExternalStorageEnabled()){
                freeBytes = new File(context.getExternalFilesDir(null).toString()).getFreeSpace();
            }
            long Gb = 1073741824L; //1gb in bytes
            double availableSpaceinGB = (double) freeBytes / Gb;
            if(availableSpaceinGB < MIN_FREE_STORAGE){
                lowOnStorage = true;
            }
        }catch(Exception e){
            LOGGER.e(e, e.getMessage());
        }
        return lowOnStorage;
    }

    private boolean isExternalStorageEnabled(){
        SettingsDao settingsDao = new SettingsDao();
        Settings settings = settingsDao.getSettings();
        return settings.isExternalStorageEnabled();
    }
}
