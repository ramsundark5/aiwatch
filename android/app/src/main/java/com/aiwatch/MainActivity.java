package com.aiwatch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.aiwatch.firebase.FirebaseUserDataManager;
import com.facebook.react.ReactActivity;

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

    private void registerFCMToken(){
        FirebaseUserDataManager firebaseUserDataManager = new FirebaseUserDataManager();
        firebaseUserDataManager.registerFCMToken(getApplicationContext());
    }
}
