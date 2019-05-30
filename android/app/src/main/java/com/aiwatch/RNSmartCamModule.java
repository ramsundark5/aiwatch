package com.aiwatch;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.aiwatch.common.AppConstants;
import com.aiwatch.media.db.Settings;
import com.aiwatch.media.db.SettingsDao;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.Gson;
import com.aiwatch.common.ConversionUtil;
import com.aiwatch.media.db.AlarmEvent;
import com.aiwatch.media.db.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.media.db.AlarmEventDao;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class RNSmartCamModule extends ReactContextBaseJavaModule {
    private static final Logger LOGGER = new Logger();
    private final Context reactContext;
    private Gson gson;
    private LocalBroadcastReceiver  mLocalBroadcastReceiver;

    public RNSmartCamModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()))
                .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (date, type, jsonSerializationContext) -> new JsonPrimitive(date.getTime()))
                .create();
        this.mLocalBroadcastReceiver = new LocalBroadcastReceiver();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(reactContext);
        localBroadcastManager.registerReceiver(mLocalBroadcastReceiver, new IntentFilter(AppConstants.AIWATCH_EVENT_INTENT));
    }

    @ReactMethod
    public void getAllCameras(final Promise promise) {
        try {
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            List<CameraConfig> cameras = cameraConfigDao.getAllCameras();
            String jsonString = gson.toJson(cameras);
            JSONArray jsonArray = new JSONArray(jsonString);
            WritableArray cameraArray = ConversionUtil.convertJsonToArray(jsonArray);
            promise.resolve(cameraArray);
        } catch (Exception e) {
            promise.reject(e);
            LOGGER.e(e.getMessage());
        }
    }

    @ReactMethod
    public void putCamera(final ReadableMap readableMap, final Promise promise) {
        try {
            JSONObject jsonObject = ConversionUtil.convertMapToJson(readableMap);
            CameraConfig cameraConfig = gson.fromJson(jsonObject.toString(), CameraConfig.class);
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            cameraConfigDao.putCamera(cameraConfig);
            if(cameraConfig.getId() == 0){
                throw new Exception("Error trying to save Camera Info. Please try again.");
            }
            Intent intent = new Intent(reactContext, MonitoringService.class);
            intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.SAVE_CAMERA);
            intent.putExtra(AppConstants.CAMERA_CONFIG_EXTRA, cameraConfig);
            reactContext.startService(intent);
            promise.resolve(Long.valueOf(cameraConfig.getId()).intValue());
        } catch (Exception e) {
            promise.reject(e);
            LOGGER.e(e.getMessage());
        }
    }

    @ReactMethod
    public void deleteCamera(Integer cameraId, final Promise promise) {
        try {
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            cameraConfigDao.deleteCamera(cameraId);
            AlarmEventDao alarmEventDao = new AlarmEventDao();
            alarmEventDao.deleteEventsForCamera(cameraId);
            Intent intent = new Intent(reactContext, MonitoringService.class);
            intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.REMOVE_CAMERA);
            intent.putExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, cameraId);
            promise.resolve("camera deleted and object detection stopped");
        } catch (Exception e) {
            promise.reject(e);
            LOGGER.e(e.getMessage());
        }
    }

    @ReactMethod
    public void getEventsForDateRange(final ReadableMap dates, final Promise promise) {
        try {
            AlarmEventDao alarmEventDao = new AlarmEventDao();
            long endDate = (long) dates.getDouble("endDate");
            long startDate = (long) dates.getDouble("startDate");
            List<AlarmEvent> alarmEvents = alarmEventDao.getEventsForDateRange(new Date(endDate), new Date(startDate));
            String jsonString = gson.toJson(alarmEvents);
            JSONArray jsonArray = new JSONArray(jsonString);
            WritableArray eventsArray = ConversionUtil.convertJsonToArray(jsonArray);
            promise.resolve(eventsArray);
        } catch (Exception e) {
            promise.reject(e);
            LOGGER.e(e.getMessage());
        }
    }

    @ReactMethod
    public void deleteEvents(ReadableArray eventIdJsArray, final Promise promise) {
        if(eventIdJsArray == null || eventIdJsArray.size() <= 0){
            promise.resolve("nothing to delete");
        }
        try {
            AlarmEventDao alarmEventDao = new AlarmEventDao();
            List<Object> eventIdList = eventIdJsArray.toArrayList();
            for(Object eventId : eventIdList){
                alarmEventDao.deleteEvent(((Double)(eventId)).longValue());
            }
            LOGGER.d("Events deleted");
            promise.resolve("Events deleted");
        } catch (Exception e) {
            promise.reject(e);
            LOGGER.e(e.getMessage());
        }
    }

    @ReactMethod
    public void putSettings(final ReadableMap readableMap, final Promise promise) {
        try {
            JSONObject jsonObject = ConversionUtil.convertMapToJson(readableMap);
            Settings settings = gson.fromJson(jsonObject.toString(), Settings.class);
            SettingsDao settingsDao = new SettingsDao();
            settingsDao.putSettings(settings);
            if(settings.getId() == 0){
                throw new Exception("Error trying to save settings. Please try again.");
            }
            promise.resolve(Long.valueOf(settings.getId()).intValue());
        } catch (Exception e) {
            promise.reject(e);
            LOGGER.e(e.getMessage());
        }
    }

    @ReactMethod
    public void getSettings(final Promise promise) {

        try {
            SettingsDao settingsDao = new SettingsDao();
            Settings settings = settingsDao.getSettings();
            String jsonString = gson.toJson(settings);
            JSONObject jsonObject = new JSONObject(jsonString);
            WritableMap settingsMap = ConversionUtil.convertJsonToMap(jsonObject);
            promise.resolve(settingsMap);
        } catch (Exception e) {
            promise.reject(e);
            LOGGER.e(e.getMessage());
        }
    }

    @ReactMethod
    public void isMonitoringServiceRunning(final Promise promise){
        boolean isRunning = false;
        ActivityManager manager = (ActivityManager) reactContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MonitoringService.class.getName().equals(service.service.getClassName())) {
                LOGGER.i ("Monitoring service is running");
                isRunning = true;
                break;
            }
        }
        LOGGER.i ("Monitoring service is NOT running");
        promise.resolve(isRunning);
    }

    @ReactMethod
    public void toggleMonitoringStatus(boolean enableMonitoring, final Promise promise){
        Intent intent = new Intent(reactContext, MonitoringService.class);
        if(enableMonitoring){
            intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.START_MONITORING);
            reactContext.startService(intent);
        }else{
            intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.STOP_MONITORING);
            reactContext.startService(intent);
        }
    }

    public class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context applicationContext, Intent intent) {
            try{
                ReactApplicationContext reactApplicationContext = (ReactApplicationContext) reactContext;

                AlarmEvent alarmEvent = (AlarmEvent) intent.getSerializableExtra(AppConstants.NEW_DETECTION_EVENT);
                if(alarmEvent != null){
                    String jsonString = gson.toJson(alarmEvent);
                    JSONObject jsonObject = new JSONObject(jsonString);
                    WritableMap alarmEventMap = ConversionUtil.convertJsonToMap(jsonObject);
                    reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(AppConstants.NEW_DETECTION_JS_EVENT, alarmEventMap);
                } else{
                    CameraConfig cameraConfig = (CameraConfig) intent.getSerializableExtra(AppConstants.STATUS_CHANGED_EVENT);
                    if(cameraConfig != null){
                        String jsonString = gson.toJson(cameraConfig);
                        JSONObject jsonObject = new JSONObject(jsonString);
                        WritableMap camerConfigMap = ConversionUtil.convertJsonToMap(jsonObject);
                        reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(AppConstants.STATUS_CHANGED_EVENT_JS_EVENT, camerConfigMap);
                    }
                }
            }catch(Exception e){
                LOGGER.e("Exception notifying UI about events "+e.getMessage());
            }
        }
    }

    @Override
    public String getName() {
        return "RNSmartCam";
    }
}
