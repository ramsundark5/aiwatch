package com.aiwatch.firebase;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.cloud.gdrive.GDriveServiceHelper;
import com.aiwatch.cloud.gdrive.GdriveManager;
import com.aiwatch.media.db.AlarmEvent;
import com.aiwatch.media.db.AlarmEventDao;
import com.aiwatch.postprocess.RecordingManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FirebaseAlarmEventManager {

    private static final Logger LOGGER = new Logger();
    private AlarmEventDao alarmEventDao = new AlarmEventDao();

    public void getAlarmEventUpdates(FirebaseUser firebaseUser, Context context){
        try{
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            AlarmEvent latestAlarmEvent = alarmEventDao.getLatestAlarmEvent();
            Date lastUpdatedDate = new Date(0L);
            if(latestAlarmEvent != null && latestAlarmEvent.getDate() != null){
                lastUpdatedDate = latestAlarmEvent.getDate();
            }
            final DocumentReference userRef = db.collection("users").document(firebaseUser.getUid());

            Task<QuerySnapshot> querySnapshotTask = userRef.collection("events")
                    .whereGreaterThan("date", lastUpdatedDate)
                    .get();
            QuerySnapshot querySnapshots = Tasks.await(querySnapshotTask);
            handleAlarmEventUpdates(querySnapshots, context);
        }catch(Exception e){
            LOGGER.e(e, "Error getting alarmevent updates");
        }
    }

    protected void handleAlarmEventUpdates(QuerySnapshot querySnapshots, Context context){
        for (DocumentChange dc : querySnapshots.getDocumentChanges()) {
            QueryDocumentSnapshot alarmEventSnapshot = dc.getDocument();
            if (alarmEventSnapshot == null || !alarmEventSnapshot.exists()) {
                LOGGER.d("Current data: null");
                continue;
            }
            try{
                AlarmEvent alarmEvent = alarmEventSnapshot.toObject(AlarmEvent.class);
                AlarmEvent existingAlarmEvent = alarmEventDao.getEventByUUID(alarmEvent.getUuid());
                switch (dc.getType()) {
                    case ADDED:
                        if(existingAlarmEvent == null){
                            //alarm event doesn't exist, so add it
                            addNewAlarmEvent(alarmEvent, context);
                        }
                        break;
                    case REMOVED:
                        if(existingAlarmEvent != null){
                            alarmEventDao.deleteEvent(existingAlarmEvent.getId());
                        }
                        break;
                }
            }catch (Exception ex){
                LOGGER.e(ex, "Exception handling alarmevent updates");
            }

        }
    }

    private void addNewAlarmEvent(AlarmEvent alarmEvent, Context context){
        alarmEvent.setId(0L);
        saveFileLocally(alarmEvent, context);
        alarmEventDao.putEvent(alarmEvent);
    }

    private void saveFileLocally(AlarmEvent alarmEvent, Context context){
        try{
            GDriveServiceHelper gDriveServiceHelper = GdriveManager.getGDriveServiceHelper(context);
            if(gDriveServiceHelper == null){
                return;
            }
            String filePath = getFilePathToRecord(alarmEvent, context);
            String imageDownloadPath = filePath + RecordingManager.DEFAULT_IMAGE_EXTENSION;
            String videoDownloadPath = filePath + RecordingManager.DEFAULT_VIDEO_EXTENSION;
            if(alarmEvent.getCloudImagePath() != null && !alarmEvent.getCloudImagePath().isEmpty()){
                gDriveServiceHelper.downloadFile(alarmEvent.getCloudImagePath(), imageDownloadPath);
                alarmEvent.setThumbnailPath(imageDownloadPath);
            }
            if(alarmEvent.getCloudVideoPath() != null && !alarmEvent.getCloudVideoPath().isEmpty()){
                gDriveServiceHelper.downloadFile(alarmEvent.getCloudVideoPath(), videoDownloadPath);
                alarmEvent.setVideoPath(videoDownloadPath);
            }
        }catch(Exception e){
            LOGGER.e(e, "Error downloading file from gdrive");
        }
    }

    public static String getFilePathToRecord(AlarmEvent alarmEvent, Context context){
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmm");
        String currentTime = dateFormat.format(alarmEvent.getDate());
        String fileName = alarmEvent.getCameraId() + currentTime;
        File outputFile = new File(context.getFilesDir(), fileName);
        return outputFile.getAbsolutePath();
    }
}
