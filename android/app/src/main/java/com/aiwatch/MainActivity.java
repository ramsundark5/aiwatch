package com.aiwatch;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import org.devio.rn.splashscreen.SplashScreen;

import com.aiwatch.common.AppConstants;
import com.aiwatch.firebase.FirebaseUserDataDao;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      Intent intent = new Intent("onConfigurationChanged");
      intent.putExtra("newConfig", newConfig);
      this.sendBroadcast(intent);
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
        stopMOnitoringDisabledCameras();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMOnitoringDisabledCameras();
    }

    private void stopMOnitoringDisabledCameras(){
        try{
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            List<CameraConfig> cameraConfigs = cameraConfigDao.getAllCameras();
            for(CameraConfig cameraConfig: cameraConfigs){
                if(cameraConfig.isLiveHLSViewEnabled() ){
                    cameraConfig.setLiveHLSViewEnabled(false);
                    cameraConfigDao.putCamera(cameraConfig);
                }
                if(!cameraConfig.isMonitoringEnabled() && !cameraConfig.isCvrEnabled()) {
                    Intent intent = new Intent(getApplicationContext(), MonitoringService.class);
                    intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.DISCONNECT_CAMERA);
                    intent.putExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, cameraConfig.getId());
                    getApplicationContext().startService(intent);
                }
            }
        }catch (Exception e){
            LOGGER.e("Error stopping hlslive view "+e);
        }
    }
}


