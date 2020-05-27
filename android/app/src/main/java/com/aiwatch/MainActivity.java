package com.aiwatch;

import android.content.Intent;
import android.os.Bundle;

import org.devio.rn.splashscreen.SplashScreen;
import com.aiwatch.firebase.FirebaseUserDataDao;
import com.aiwatch.media.DetectionController;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.models.CameraConfig;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;

import java.util.List;

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

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Override
            protected ReactRootView createRootView() {
                return new RNGestureHandlerEnabledRootView(MainActivity.this);
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            List<CameraConfig> cameraConfigs = cameraConfigDao.getAllCameras();
            for(CameraConfig cameraConfig: cameraConfigs){
                if(cameraConfig.isLiveHLSViewEnabled() &&
                        !cameraConfig.isMonitoringEnabled() &&
                        !cameraConfig.isCvrEnabled()){
                    cameraConfig.setLiveHLSViewEnabled(false);
                    cameraConfigDao.putCamera(cameraConfig);
                    DetectionController detectionController = new DetectionController();
                    detectionController.stopDetecting(cameraConfig.getId());
                }
            }
        }catch (Exception e){
            LOGGER.e("Error stopping hlslive view "+e);
        }
    }
}


