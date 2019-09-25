package com.aiwatch.common;

import android.content.Context;
import android.os.Environment;
import com.aiwatch.Logger;
import java.io.File;

public class FileUtil {

    private static final Logger LOGGER = new Logger();

    public static File getBaseDirectory(Context context, String folderName, String mediaType){
        File outputDir = new File(context.getFilesDir(), folderName);
        if(isExternalStorageReadable() && isExternalStorageWritable()){
            outputDir = getExternalStoragePath(context, folderName, mediaType);
        }
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }
        return outputDir;
    }

    public static File getExternalStoragePath(Context context, String fileName, String mediaType) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), fileName);
        if (!file.mkdirs()) {
            LOGGER.e("Unable to create directory");
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
