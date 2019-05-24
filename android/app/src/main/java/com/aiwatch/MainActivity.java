package com.aiwatch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.aiwatch.common.AppConstants;
import com.aiwatch.media.CompressionService;
import com.facebook.react.ReactActivity;

public class MainActivity extends ReactActivity {

    private static final Logger LOGGER = new Logger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        startMonitoringService();
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
            Intent intent = new Intent(getApplicationContext(), MonitoringService.class);
            //intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.START_MONITORING);
            ContextCompat.startForegroundService(getApplicationContext(), intent);
            LOGGER.d("Invoking monitoring from main activity");
        }catch(Exception e){
            LOGGER.e("error starting monitoring service "+e.getMessage());
        }
    }
}
