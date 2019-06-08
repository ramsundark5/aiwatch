package com.aiwatch.firebase;

import android.content.Context;
import android.support.annotation.NonNull;

import com.aiwatch.Logger;
import com.aiwatch.media.db.AlarmEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseNotificationDao {

    private static final Logger LOGGER = new Logger();
    private FirebaseAuthManager firebaseAuthManager = new FirebaseAuthManager();
    private FirebaseUserDataDao firebaseUserDataDao = new FirebaseUserDataDao();

    /*
     * Do not call this function from the main thread. Otherwise,
     * an IllegalStateException will be thrown.
     */
    public void sendPushNotification(Context context, AlarmEvent alarmEvent){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                FirebaseUser firebaseUser = firebaseAuthManager.getFirebaseUser(context);
                String adInfoId = firebaseUserDataDao.getAdInfoId(context);
                alarmEvent.setDeviceId(adInfoId);
                alarmEvent.setUserId(firebaseUser.getUid());
                addNotificationDocument(firebaseUser, alarmEvent);
            } catch (Exception e) {
                LOGGER.e(e, "Error adding notification record");
            }
        });
    }

    private void addNotificationDocument(FirebaseUser firebaseUser, AlarmEvent alarmEvent){
        if(firebaseUser != null && firebaseUser.getUid() != null){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("notifications").add(alarmEvent)
                    .addOnSuccessListener(documentReference -> LOGGER.d("New notification document added"))
                    .addOnFailureListener(e -> LOGGER.e(e, "Error adding notification firebase record"));
        }
    }
}
