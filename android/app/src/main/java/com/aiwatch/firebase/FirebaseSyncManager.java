package com.aiwatch.firebase;

import android.content.Context;

import com.aiwatch.Logger;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;

import java.util.List;

public class FirebaseSyncManager {
    private static final Logger LOGGER = new Logger();

    public void sync(Context context){
        syncCameras(context);
    }

    protected void syncCameras(Context context){
        CameraConfigDao cameraConfigDao = new CameraConfigDao();
        FirebaseCameraConfigDao firebaseCameraConfigDao = new FirebaseCameraConfigDao();
        try{
            List<CameraConfig> cameras = cameraConfigDao.getAllCameras();
            for(CameraConfig cameraConfig : cameras){
                firebaseCameraConfigDao.putCamera(context, cameraConfig);
            }
        }catch (Exception e){
            LOGGER.e(e, e.getMessage());
        }
    }
}
