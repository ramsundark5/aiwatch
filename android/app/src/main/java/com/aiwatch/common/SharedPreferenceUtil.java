package com.aiwatch.common;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {

    public static boolean isStopMonitoringRequested(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(AppConstants.APP_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(AppConstants.STOP_MONITORING_REQUESTED, false);
    }

    public static void setStopMonitoringRequested(Context context, boolean value){
        SharedPreferences sharedPref = context.getSharedPreferences(AppConstants.APP_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(AppConstants.STOP_MONITORING_REQUESTED, value);
        editor.commit();
    }

    public static boolean isCameraMonitorRunning(Context context, long cameraId){
        SharedPreferences sharedPref = context.getSharedPreferences(AppConstants.APP_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(AppConstants.CAMERA_MONITOR_STATUS_PREFIX + cameraId, false);
    }

    public static void setCameraMonitorStatus(Context context, long cameraId, boolean value){
        SharedPreferences sharedPref = context.getSharedPreferences(AppConstants.APP_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(AppConstants.CAMERA_MONITOR_STATUS_PREFIX + cameraId, value);
        editor.commit();
    }
}
