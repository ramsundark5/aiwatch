package com.aiwatch.common;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import com.aiwatch.Logger;
import com.aiwatch.media.db.SettingsDao;
import com.aiwatch.models.Settings;

import java.io.File;

public class FileUtil {

    private static final Logger LOGGER = new Logger();

    public static File getApplicationDirectory(Context context, String folderName){
        File outputDir = new File(context.getFilesDir(), folderName);
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }
        return outputDir;
    }

    public static File getBaseDirectory(Context context, String folderName){
        File outputDir = new File(context.getFilesDir(), folderName);
        SettingsDao settingsDao = new SettingsDao();
        Settings settings = settingsDao.getSettings();
        if(settings.isGalleryAccessEnabled() && isGalleryAccessible(context)){
            outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/aiwatch", folderName);
        }

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
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), fileName);
        // Get the directory for the app's private pictures directory.
        if(isGalleryAccessible(context)){
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/aiwatch", fileName);
        }
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

    private static boolean isGalleryAccessible(Context context){
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        boolean galleryAccessible;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            galleryAccessible = context.checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid())
                    == PackageManager.PERMISSION_GRANTED;
        }else{
            galleryAccessible = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return galleryAccessible;
    }
}
