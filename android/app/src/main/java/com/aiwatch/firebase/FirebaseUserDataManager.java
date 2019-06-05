package com.aiwatch.firebase;

import android.content.Context;
import com.aiwatch.Logger;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseUserDataManager {

    private static final Logger LOGGER = new Logger();
    private FirebaseAuthManager firebaseAuthManager = new FirebaseAuthManager();

    // Do not call this function from the main thread. Otherwise,
    // an IllegalStateException will be thrown.
    public void registerToken(Context context, String token) {
        try{
           FirebaseUser firebaseUser = firebaseAuthManager.getFirebaseUser(context);
           sendTokenToDB(firebaseUser, context, token);
        }catch(Exception e){
            LOGGER.e(e, "Error registering token");
        }
    }

    public void sendTokenToDB(FirebaseUser firebaseUser, Context context, String token) {
        LOGGER.d("Refreshed token: " + token);
        if(firebaseUser != null && firebaseUser.getUid() != null){
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUserData firebaseUserData = buildUserData(firebaseUser, context, token);
            db.collection("users").document(firebaseUser.getUid()).set(firebaseUserData);
        }
    }

    private FirebaseUserData buildUserData(FirebaseUser firebaseUser, Context context, String token){
        FirebaseUserData firebaseUserData = getCurrentDataFromFirebase(firebaseUser);
        if(firebaseUserData == null){
            firebaseUserData = buildNewFirebaseUserData(firebaseUser);
        }
        Map<String, String> deviceTokens = firebaseUserData.getDeviceTokens();
        if(deviceTokens == null){
            deviceTokens = new HashMap<>();
        }
        String adInfoId = getAdInfoId(context);
        deviceTokens.put(adInfoId, token);
        firebaseUserData.setDeviceTokens(deviceTokens);
        return firebaseUserData;
    }

    public String getAdInfoId(Context context) {
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        } catch (GooglePlayServicesNotAvailableException gpe) {
            LOGGER.e(gpe, "Google play not available");
        } catch (Exception e) {
            LOGGER.e(e, "Error getting adinfo");
        }
        final String adInfoIdd = adInfo.getId();
        return adInfoIdd;
    }

    private FirebaseUserData getCurrentDataFromFirebase(FirebaseUser firebaseUser) {
        FirebaseUserData firebaseUserData = null;
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Task<DocumentSnapshot> userDataSnapshotTask = db.collection("users").document(firebaseUser.getUid()).get();
            DocumentSnapshot userDataSnapshot = null;
            userDataSnapshot = Tasks.await(userDataSnapshotTask);
            if(userDataSnapshot != null){
                firebaseUserData = userDataSnapshot.toObject(FirebaseUserData.class);
            }
        } catch (Exception e) {
            LOGGER.e(e, "Error getting current data from firebase");
        }
        return firebaseUserData;
    }

    private FirebaseUserData buildNewFirebaseUserData(FirebaseUser firebaseUser){
        FirebaseUserData userData = null;
        try{
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            userData = new FirebaseUserData();
            userData.setId(firebaseUser.getUid());
            userData.setEmail(firebaseUser.getEmail());
            List<CameraConfig> cameraConfigList = cameraConfigDao.getAllCameras();
            userData.setCameraConfigList(cameraConfigList);
        }catch(Exception e){
            LOGGER.e(e, "error building firebase user data");
        }
        return userData;
    }
}
