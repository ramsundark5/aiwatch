package com.aiwatch.firebase;

import android.content.Context;

import com.aiwatch.Logger;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.postprocess.NotificationManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

public class FirebaseCameraManager {

    private static final Logger LOGGER = new Logger();
    private CameraConfigDao cameraConfigDao = new CameraConfigDao();

    public void getCameraConfigUpdates(FirebaseUser firebaseUser, Context context){
        try{
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CameraConfig latestCameraConfig = cameraConfigDao.getLatestCameraConfig();
            Date lastUpdatedDate = new Date(0L);
            if(latestCameraConfig != null && latestCameraConfig.getLastModified() != null){
                lastUpdatedDate = latestCameraConfig.getLastModified();
            }
            final DocumentReference userRef = db.collection("users").document(firebaseUser.getUid());

            Task<QuerySnapshot> querySnapshotTask = userRef.collection("cameras")
                    .whereGreaterThan("lastModified", lastUpdatedDate)
                    .get();
            QuerySnapshot querySnapshots = Tasks.await(querySnapshotTask);
            handleCameraConfigUpdates(querySnapshots, context);
        }catch(Exception e){
            LOGGER.e(e, "Error getting cameraconfig updates");
        }
    }

    protected void handleCameraConfigUpdates(QuerySnapshot querySnapshots, Context context){
        for (DocumentChange dc : querySnapshots.getDocumentChanges()) {
            QueryDocumentSnapshot cameraConfigSnapshot = dc.getDocument();
            if (cameraConfigSnapshot == null || !cameraConfigSnapshot.exists()) {
                LOGGER.d("Current data: null");
                continue;
            }
            try{
                CameraConfig cameraConfig = cameraConfigSnapshot.toObject(CameraConfig.class);
                CameraConfig existingCameraConfig = cameraConfigDao.getCameraByUUID(cameraConfig.getUuid());
                switch (dc.getType()) {
                    case ADDED:
                        if(existingCameraConfig == null) {
                            existingCameraConfig = new CameraConfig();
                            existingCameraConfig.setId(0L);
                        }
                        applyModificationsToCameraConfig(existingCameraConfig, cameraConfig, context);
                        break;
                    case MODIFIED:
                        applyModificationsToCameraConfig(existingCameraConfig, cameraConfig, context);
                        break;
                    case REMOVED:
                        if(existingCameraConfig != null){
                            cameraConfigDao.deleteCamera(existingCameraConfig.getId());
                        }
                        break;
                }
            }catch (Exception ex){
                LOGGER.e(ex, "Exception handling cameraconfig updates");
            }

        }
    }

    private void applyModificationsToCameraConfig(CameraConfig existingCameraConfig, CameraConfig updatedCameraConfig, Context context){
        existingCameraConfig.setVideoUrl(updatedCameraConfig.getVideoUrl());
        existingCameraConfig.setBrand(updatedCameraConfig.getBrand());
        existingCameraConfig.setModel(updatedCameraConfig.getModel());
        existingCameraConfig.setName(updatedCameraConfig.getName());
        existingCameraConfig.setUsername(updatedCameraConfig.getUsername());
        existingCameraConfig.setLastModified(updatedCameraConfig.getLastModified());
        existingCameraConfig.setUuid(updatedCameraConfig.getUuid());
        //existingCameraConfig.setPassword(updatedCameraConfig.getPassword());
        existingCameraConfig.setVideoCodec(updatedCameraConfig.getVideoCodec());
        cameraConfigDao.putCamera(existingCameraConfig);
        NotificationManager.sendUINotification(context, existingCameraConfig);
    }
}
