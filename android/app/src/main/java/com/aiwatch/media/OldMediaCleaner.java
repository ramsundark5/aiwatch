package com.aiwatch.media;

import android.content.Context;

import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class OldMediaCleaner {
    private static final Logger LOGGER = new Logger();

    public void cleanupMedia(Context context){
        LOGGER.d("Starting old files cleanup");
        try{
            File videoFolder = new File(context.getFilesDir(), AppConstants.TEMP_VIDEO_FOLDER);
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
}
