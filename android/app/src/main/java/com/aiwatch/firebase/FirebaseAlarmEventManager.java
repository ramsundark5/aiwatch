package com.aiwatch.firebase;

import com.aiwatch.Logger;
import com.aiwatch.media.db.AlarmEvent;
import com.aiwatch.media.db.AlarmEventDao;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

public class FirebaseAlarmEventManager {

    private static final Logger LOGGER = new Logger();
    private AlarmEventDao alarmEventDao = new AlarmEventDao();

    public void getAlarmEventUpdates(FirebaseUser firebaseUser){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference userRef = db.collection("users").document(firebaseUser.getUid());

        userRef.collection("events").whereGreaterThanOrEqualTo("date", new Date()).get()
                .addOnSuccessListener(querySnapshots -> handleAlarmEventUpdates(querySnapshots))
                .addOnFailureListener(e -> LOGGER.e(e, "Error getting alarmevent updates"));
    }

    protected void handleAlarmEventUpdates(QuerySnapshot querySnapshots){
        for (DocumentChange dc : querySnapshots.getDocumentChanges()) {
            QueryDocumentSnapshot alarmEventSnapshot = dc.getDocument();
            if (alarmEventSnapshot == null || !alarmEventSnapshot.exists()) {
                LOGGER.d("Current data: null");
                continue;
            }
            try{
                AlarmEvent alarmEvent = alarmEventSnapshot.toObject(AlarmEvent.class);
                switch (dc.getType()) {
                    case ADDED:
                        alarmEventDao.putEvent(alarmEvent);
                        break;
                    case REMOVED:
                        alarmEventDao.deleteEvent(alarmEvent.getId());
                        break;
                }
            }catch (Exception ex){
                LOGGER.e(ex, "Exception handling alarmevent updates");
            }

        }
    }
}
