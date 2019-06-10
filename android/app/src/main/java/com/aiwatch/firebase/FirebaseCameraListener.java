package com.aiwatch.firebase;

import android.support.annotation.Nullable;

import com.aiwatch.Logger;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FirebaseCameraListener {

    private static final Logger LOGGER = new Logger();

    public void registerCameraConfigListener(FirebaseUser firebaseUser){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference userRef = db.collection("users").document(firebaseUser.getUid());
        userRef.collection("cameras").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot querySnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    LOGGER.e(e, "Camera Change Listen failed.");
                    return;
                }
                CameraConfigDao cameraConfigDao = new CameraConfigDao();
                for (DocumentChange dc : querySnapshots.getDocumentChanges()) {
                    QueryDocumentSnapshot cameraConfigSnapshot = dc.getDocument();
                    if (cameraConfigSnapshot == null || !cameraConfigSnapshot.exists()) {
                        LOGGER.d("Current data: null");
                        continue;
                    }
                    CameraConfig cameraConfig = cameraConfigSnapshot.toObject(CameraConfig.class);
                    switch (dc.getType()) {
                        case ADDED:
                            cameraConfigDao.putCamera(cameraConfig);
                            break;
                        case MODIFIED:
                            break;
                        case REMOVED:
                            cameraConfigDao.deleteCamera(cameraConfig.getId());
                            break;
                    }
                }

               /* if (cameraConfigSnapshot != null && cameraConfigSnapshot.exists()) {
                    CameraConfig cameraConfig = cameraConfigSnapshot.toObject(CameraConfig.class);
                    CameraConfigDao cameraConfigDao = new CameraConfigDao();
                    cameraConfigDao.putCamera(cameraConfig);
                } else {
                    LOGGER.d("Current data: null");
                }*/
            }
        });

    }
}
