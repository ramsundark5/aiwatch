package com.aiwatch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.aiwatch.firebase.FirebaseAuthManager;
import com.aiwatch.firebase.FirebaseCameraListener;
import com.aiwatch.firebase.FirebaseUserDataDao;
import com.facebook.react.ReactActivity;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends ReactActivity {

    private static final Logger LOGGER = new Logger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        startMonitoringService();
        registerFCMToken();
    }

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "aiwatch";
    }

    private void startMonitoringService(){
        try{
            Intent monitoringIntent = new Intent(getApplicationContext(), MonitoringService.class);
            ContextCompat.startForegroundService(getApplicationContext(), monitoringIntent);
            LOGGER.d("Invoking monitoring from main activity");
        }catch(Exception e){
            LOGGER.e("error starting monitoring service "+e.getMessage());
        }
    }

    private void registerFirebaseListeners(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                FirebaseAuthManager firebaseAuthManager = new FirebaseAuthManager();
                FirebaseUser firebaseUser = firebaseAuthManager.getFirebaseUser(getApplicationContext());
                FirebaseCameraListener firebaseCameraListener = new FirebaseCameraListener();
                firebaseCameraListener.registerCameraConfigListener(firebaseUser);
            } catch (Exception e) {
                LOGGER.e(e, "Error registering token");
            }
        });
    }

    private void registerFCMToken(){
        FirebaseUserDataDao firebaseUserDataDao = new FirebaseUserDataDao();
        firebaseUserDataDao.registerFCMToken(getApplicationContext());
    }
}
