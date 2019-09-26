package com.aiwatch.common;

import android.content.Context;
import android.os.Environment;
import com.aiwatch.Logger;
import com.aiwatch.media.db.SettingsDao;
import com.aiwatch.models.Settings;

import java.io.File;

public class FileUtil {

    private static final Logger LOGGER = new Logger();

    public static File getBaseDirectory(Context context, String folderName){
        File outputDir = new File(context.getFilesDir(), folderName);
        SettingsDao settingsDao = new SettingsDao();
        Settings settings = settingsDao.getSettings();
        if(settings.isExternalStorageEnabled()){
            if(isExternalStorageReadable() && isExternalStorageWritable()){
                outputDir = getExternalStoragePath(context, folderName);
            }
        }
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }
        return outputDir;
    }

    public static File getExternalStoragePath(Context context, String fileName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), fileName);
        if (!file.exists()) {
            boolean folderCreated = file.mkdirs();
            if(!folderCreated){
                LOGGER.e("Unable to create directory "+ fileName);
            }
        }
        return file;
    }


    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
