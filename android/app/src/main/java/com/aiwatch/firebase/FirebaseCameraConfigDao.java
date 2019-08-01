package com.aiwatch.firebase;

import android.content.Context;

import com.aiwatch.Logger;
import com.aiwatch.models.CameraConfig;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseCameraConfigDao {

    private static final Logger LOGGER = new Logger();
    private FirebaseAuthManager firebaseAuthManager = new FirebaseAuthManager();

    public void putCamera(Context context, CameraConfig cameraConfig){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                FirebaseUser firebaseUser = firebaseAuthManager.getFirebaseUser(context);
                if(firebaseUser == null){
                    return;
                }
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                //scrub password
                cameraConfig.setPassword(null);
                db.collection("users")
                        .document(firebaseUser.getUid())
                        .collection("cameras")
                        .document(String.valueOf(cameraConfig.getUuid()))
                        .set(cameraConfig)
                        .addOnSuccessListener(documentReference -> LOGGER.d("Camera updated to firebase"))
                        .addOnFailureListener(e -> LOGGER.e(e, "Failed updating camera to firebase"));

            } catch (Exception e) {
                LOGGER.e(e, "Error updating camera to firebase");
            }
        });
    }

    public void deleteCamera(Context context, String cameraUUId){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                FirebaseUser firebaseUser = firebaseAuthManager.getFirebaseUser(context);
                if(firebaseUser == null){
                    return;
                }
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(firebaseUser.getUid())
                        .collection("cameras")
                        .document(cameraUUId)
                        .delete()
                        .addOnSuccessListener(documentReference -> LOGGER.d("Camera deleted from firebase"))
                        .addOnFailureListener(e -> LOGGER.e(e, "Failed deleting Camera from firebase"));
            } catch (Exception e) {
                LOGGER.e(e, "Error deleting camera from firebase");
            }
        });
    }

}
