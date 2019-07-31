package com.aiwatch.firebase;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.media.db.AlarmEvent;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirebaseAlarmEventDao {

    private static final Logger LOGGER = new Logger();
    private FirebaseAuthManager firebaseAuthManager = new FirebaseAuthManager();

    public void addEvent(Context context, AlarmEvent alarmEvent){
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
                        .collection("events")
                        .document(String.valueOf(alarmEvent.getUuid()))
                        .set(alarmEvent)
                        .addOnSuccessListener(documentReference -> LOGGER.d("Alarmevent added to firebase"))
                        .addOnFailureListener(e -> LOGGER.e(e, "Failed adding alarmevent to firebase"));

            } catch (Exception e) {
                LOGGER.e(e, "Error adding alarmevent to firebase");
            }
        });
    }

    public void deleteEvents(Context context, List<AlarmEvent> eventIdList){

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                FirebaseUser firebaseUser = firebaseAuthManager.getFirebaseUser(context);
                if(firebaseUser == null){
                    return;
                }
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference eventCollectionRef = db.collection("users")
                        .document(firebaseUser.getUid())
                        .collection("events");
                for(AlarmEvent alarmEvent: eventIdList){
                    eventCollectionRef.document(alarmEvent.getUuid())
                            .delete()
                            .addOnSuccessListener(documentReference -> LOGGER.d("Alarmevent deleted from firebase"))
                            .addOnFailureListener(e -> LOGGER.e(e, "Failed deleting alarmevent from firebase"));
                }
            } catch (Exception e) {
                LOGGER.e(e, "Error deleting alarmevent from firebase");
            }
        });
    }
}
