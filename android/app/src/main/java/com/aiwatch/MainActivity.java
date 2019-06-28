package com.aiwatch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import org.devio.rn.splashscreen.SplashScreen;
import com.aiwatch.firebase.FirebaseUserDataDao;
import com.facebook.react.ReactActivity;

public class MainActivity extends ReactActivity {

    private static final Logger LOGGER = new Logger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.show(this);
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
            startService(monitoringIntent);
            LOGGER.d("Invoking monitoring from main activity");
        }catch(Exception e){
            LOGGER.e("error starting monitoring service "+e.getMessage());
        }
    }

    private void registerFCMToken(){
        FirebaseUserDataDao firebaseUserDataDao = new FirebaseUserDataDao();
        firebaseUserDataDao.registerFCMToken(getApplicationContext());
    }
}
