package com.aiwatch.firebase;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.aiwatch.media.db.AlarmEvent;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseAlarmEventDao {

    private static final Logger LOGGER = new Logger();
    private FirebaseAuthManager firebaseAuthManager = new FirebaseAuthManager();

    public void execute(Context context, AlarmEvent alarmEvent, String action){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                FirebaseUser firebaseUser = firebaseAuthManager.getFirebaseUser(context);
                switch(action){
                    case AppConstants.ADD_ALARM_EVENT:
                        addEvent(firebaseUser, alarmEvent);
                        break;
                    case AppConstants.DELETE_ALARM_EVENT:
                        deleteEvent(firebaseUser, alarmEvent);
                        break;
                }
            } catch (Exception e) {
                LOGGER.e(e, "Error registering token");
            }
        });
    }

    public void addEvent(FirebaseUser firebaseUser, AlarmEvent alarmEvent){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference alarmEventRef = db.collection("events").document(firebaseUser.getUid());

        // Atomically add a new alarmEvent to the "events" array field.
        alarmEventRef.update("events", FieldValue.arrayUnion(alarmEvent));
        LOGGER.d("Alarmevent synced to firebase");
    }

    public void deleteEvent(FirebaseUser firebaseUser, AlarmEvent alarmEvent){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference alarmEventRef = db.collection("events").document(firebaseUser.getUid());

        // Atomically remove a new alarmEvent from the "events" array field.
        alarmEventRef.update("events", FieldValue.arrayRemove(alarmEvent));
        LOGGER.d("Alarmevent synced to firebase");
    }
}
