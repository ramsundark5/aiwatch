package com.aiwatch.firebase;

import android.content.Context;

import com.aiwatch.Logger;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseSyncManager {
    private static final Logger LOGGER = new Logger();

    public void sync(Context context){
        syncCameras(context);
    }

    protected void syncCameras(Context context){
        CameraConfigDao cameraConfigDao = new CameraConfigDao();
        FirebaseCameraConfigDao firebaseCameraConfigDao = new FirebaseCameraConfigDao();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try{
                FirebaseAuthManager firebaseAuthManager = new FirebaseAuthManager();
                FirebaseUser firebaseUser = firebaseAuthManager.getFirebaseUser(context);
                if(firebaseUser != null){
                    List<CameraConfig> cameras = cameraConfigDao.getAllCameras();
                    for(CameraConfig cameraConfig : cameras){
                        firebaseCameraConfigDao.putCamera(firebaseUser, cameraConfig);
                    }
                }
            }catch (Exception e){
                LOGGER.e(e, e.getMessage());
            }
        });
    }

    public void getFirebaseUpdates(Context context){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                FirebaseAuthManager firebaseAuthManager = new FirebaseAuthManager();
                FirebaseUser firebaseUser = firebaseAuthManager.getFirebaseUser(context);
                if(firebaseUser != null){
                    FirebaseCameraManager firebaseCameraManager = new FirebaseCameraManager();
                    firebaseCameraManager.getCameraConfigUpdates(firebaseUser, context);
                    FirebaseAlarmEventManager firebaseAlarmEventManager = new FirebaseAlarmEventManager();
                    firebaseAlarmEventManager.getAlarmEventUpdates(firebaseUser, context);
                }
            } catch (Exception e) {
                LOGGER.e(e, "Error getting updates from firebase");
            }
        });
    }
}
