package com.aiwatch;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.aiwatch.common.AppConstants;
import com.aiwatch.firebase.FirebaseAlarmEventDao;
import com.aiwatch.firebase.FirebaseAlarmEventManager;
import com.aiwatch.firebase.FirebaseAuthManager;
import com.aiwatch.firebase.FirebaseCameraConfigDao;
import com.aiwatch.firebase.FirebaseCameraManager;
import com.aiwatch.firebase.FirebaseSyncManager;
import com.aiwatch.firebase.FirebaseUserDataDao;
import com.aiwatch.media.FFmpegConnectionTester;
import com.aiwatch.models.Settings;
import com.aiwatch.media.db.SettingsDao;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.aiwatch.common.ConversionUtil;
import com.aiwatch.models.AlarmEvent;
import com.aiwatch.models.CameraConfig;
import com.aiwatch.media.db.CameraConfigDao;
import com.aiwatch.media.db.AlarmEventDao;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.evercam.network.CustomEvercamDiscover;
import io.evercam.network.DiscoveryResult;

public class RNSmartCamModule extends ReactContextBaseJavaModule {
    private static final Logger LOGGER = new Logger();
    private final Context reactContext;
    private Gson gson;
    private LocalBroadcastReceiver mLocalBroadcastReceiver;
    private FirebaseAlarmEventDao firebaseAlarmEventDao;
    private FirebaseCameraConfigDao firebaseCameraConfigDao;

    public RNSmartCamModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.firebaseAlarmEventDao = new FirebaseAlarmEventDao();
        this.firebaseCameraConfigDao = new FirebaseCameraConfigDao();
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
            getFirebaseUpdates();
        } catch (Exception e) {
            promise.reject(e);
            LOGGER.e(e.getMessage());
        }
    }

    @ReactMethod
    public void putCamera(final ReadableMap readableMap, final Promise promise) {
        try {
            //save cameraconfig
            JSONObject jsonObject = ConversionUtil.convertMapToJson(readableMap);
            CameraConfig cameraConfig = gson.fromJson(jsonObject.toString(), CameraConfig.class);
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            cameraConfig.setLastModified(new Date());
            cameraConfigDao.putCamera(cameraConfig);
            if (cameraConfig.getId() == 0) {
                throw new Exception("Error trying to save Camera Info. Please try again.");
            }

            //start or stop service
            Intent intent = new Intent(reactContext, MonitoringService.class);
            intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.SAVE_CAMERA);
            intent.putExtra(AppConstants.CAMERA_CONFIG_EXTRA, cameraConfig);
            reactContext.startService(intent);

            //return updated cameraconfig to UI
            cameraConfig.setVideoUrlWithAuth(cameraConfig.getVideoUrlWithAuth());
            String jsonString = gson.toJson(cameraConfig);
            JSONObject updatedJsonObject = new JSONObject(jsonString);
            WritableMap cameraConfigMap = ConversionUtil.convertJsonToMap(updatedJsonObject);
            promise.resolve(cameraConfigMap);

            //sync with firebase
            firebaseCameraConfigDao.putCamera(reactContext, cameraConfig);
        } catch (Exception e) {
            promise.reject(e);
            LOGGER.e(e.getMessage());
        }
    }

    @ReactMethod
    public void deleteCamera(Integer cameraId, final Promise promise) {
        try {
            CameraConfigDao cameraConfigDao = new CameraConfigDao();
            CameraConfig cameraConfig = cameraConfigDao.getCamera(cameraId);
            AlarmEventDao alarmEventDao = new AlarmEventDao();
            alarmEventDao.deleteEventsForCamera(cameraId);

            Intent intent = new Intent(reactContext, MonitoringService.class);
            intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.REMOVE_CAMERA);
            intent.putExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, cameraId);

            promise.resolve("camera deleted and object detection stopped");
            cameraConfigDao.deleteCamera(cameraId);
            firebaseCameraConfigDao.deleteCamera(reactContext, cameraConfig.getUuid());
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
            getFirebaseUpdates();
        } catch (Exception e) {
            promise.reject(e);
            LOGGER.e(e.getMessage());
        }
    }

    @ReactMethod
    public void deleteEvents(ReadableArray eventsJsArray, final Promise promise) {
        if (eventsJsArray == null || eventsJsArray.size() <= 0) {
            promise.resolve("nothing to delete");
        }
        try {
            AlarmEventDao alarmEventDao = new AlarmEventDao();
            JSONArray eventsJsonArray = ConversionUtil.convertArrayToJson(eventsJsArray);
            TypeToken<List<AlarmEvent>> alarmEventTypeToken = new TypeToken<List<AlarmEvent>>(){};
            List<AlarmEvent> eventList = gson.fromJson(eventsJsonArray.toString(), alarmEventTypeToken.getType());
            for (AlarmEvent alarmEvent : eventList) {
                alarmEventDao.deleteEvent(alarmEvent.getId());
            }
            LOGGER.d("Events deleted");
            promise.resolve("Events deleted");
            firebaseAlarmEventDao.deleteEvents(reactContext, eventList);
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
            FirebaseUserDataDao firebaseUserDataDao = new FirebaseUserDataDao();
            Settings settingsBeforeSave = settingsDao.getSettings();
            settingsDao.putSettings(settings);
            if (settings.getId() == 0) {
                throw new Exception("Error trying to save settings. Please try again.");
            }

            String jsonString = gson.toJson(settings);
            JSONObject updatedJsonObject = new JSONObject(jsonString);
            WritableMap settingsMap = ConversionUtil.convertJsonToMap(updatedJsonObject);
            promise.resolve(settingsMap);

            //resync if google account just got connected
            if(settings.isGoogleAccountConnected() && !settingsBeforeSave.isGoogleAccountConnected()){
                FirebaseSyncManager firebaseSyncManager = new FirebaseSyncManager();
                firebaseSyncManager.sync(reactContext);
                getFirebaseUpdates();
                firebaseUserDataDao.registerFCMToken(reactContext);
            }
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
            if(settings == null){
                settings = new Settings();
            }
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
    public void isMonitoringServiceRunning(final Promise promise) {
        boolean isRunning = false;
        ActivityManager manager = (ActivityManager) reactContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MonitoringService.class.getName().equals(service.service.getClassName())) {
                LOGGER.i("Monitoring service is running");
                isRunning = true;
                break;
            }
        }
        if(!isRunning){
            LOGGER.i("Monitoring service is NOT running");
        }
        promise.resolve(isRunning);
    }

    @ReactMethod
    public void toggleMonitoringStatus(boolean enableMonitoring, final Promise promise) {
        try{
            Intent intent = new Intent(reactContext, MonitoringService.class);
            if (enableMonitoring) {
                intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.START_MONITORING);
                reactContext.startService(intent);
            } else {
                intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.STOP_MONITORING);
                reactContext.startService(intent);
            }
        }finally {
            promise.resolve("monitoring toggle completed");
        }
    }

    @ReactMethod
    public void togglCameraMonitoring(final ReadableMap readableMap, final Promise promise) {
        try {
            JSONObject jsonObject = ConversionUtil.convertMapToJson(readableMap);
            CameraConfig cameraConfig = gson.fromJson(jsonObject.toString(), CameraConfig.class);
            Intent intent = new Intent(reactContext, MonitoringService.class);
            if (cameraConfig.isDisconnected()) {
                intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.DISCONNECT_CAMERA);
                intent.putExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, cameraConfig.getId());
                reactContext.startService(intent);
            } else {
                intent.putExtra(AppConstants.ACTION_EXTRA, AppConstants.CONNECT_CAMERA);
                intent.putExtra(AppConstants.CAMERA_CONFIG_ID_EXTRA, cameraConfig.getId());
                reactContext.startService(intent);
            }
        } catch (Exception e) {
            LOGGER.e(e, "error updating camera monitoring");
        }finally {
            promise.resolve("camera status updated");
        }
    }

    @ReactMethod
    public void testCameraConnection(final ReadableMap readableMap, final Promise promise) {
        String base64Image = null;
        try {
            JSONObject jsonObject = ConversionUtil.convertMapToJson(readableMap);
            CameraConfig cameraConfig = gson.fromJson(jsonObject.toString(), CameraConfig.class);
            FFmpegConnectionTester connectionTester = new FFmpegConnectionTester();
            base64Image = connectionTester.getImageFromCamera(reactContext, cameraConfig);
        } catch (Exception e) {
            LOGGER.e(e, "error getting base64 image");
        }
        if (base64Image != null) {
            promise.resolve(base64Image);
        } else {
            promise.reject("BAD_URL", "Unable to connect to camera. Check your video url and wifi connection.");
        }
    }

    @ReactMethod
    public void saveSmartthingsAccessToken(final ReadableMap readableMap, final Promise promise) {
        try{
            SettingsDao settingsDao = new SettingsDao();
            Settings settings = settingsDao.getSettings();
            String accessToken = readableMap.getString("accessToken");
            String smartAppEndpoint = readableMap.getString("smartAppEndpoint");
            String expirationDateStr = readableMap.getString("accessTokenExpirationDate");
            Date expirationDate = convertStringToDate(expirationDateStr);
            settings.setSmartthingsAccessToken(accessToken);
            settings.setSmartAppEndpoint(smartAppEndpoint);
            settings.setSmartthingsAccessTokenExpiry(expirationDate);
            settingsDao.putSettings(settings);

            String jsonString = gson.toJson(settings);
            JSONObject updatedJsonObject = new JSONObject(jsonString);
            WritableMap settingsMap = ConversionUtil.convertJsonToMap(updatedJsonObject);
            promise.resolve(settingsMap);
        }catch(Exception e){
            LOGGER.e(e, e.getMessage());
            promise.reject(e);
        }
    }

    @ReactMethod
    public void sync(final Promise promise) {
        try{
            FirebaseSyncManager firebaseSyncManager = new FirebaseSyncManager();
            firebaseSyncManager.sync(reactContext);
            getFirebaseUpdates();
        }catch(Exception e){
            LOGGER.e(e, e.getMessage());
        } finally {
            promise.resolve("sync completed");
        }
    }

    @ReactMethod
    public void discover(final Promise promise) {
        try{
            CustomEvercamDiscover evercamDiscover = new CustomEvercamDiscover((ReactContext) reactContext);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(evercamDiscover);
        }catch(Exception e){
            LOGGER.e(e, e.getMessage());
        } finally {
            promise.resolve("started discovery");
        }
    }

    private void getFirebaseUpdates(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                FirebaseAuthManager firebaseAuthManager = new FirebaseAuthManager();
                FirebaseUser firebaseUser = firebaseAuthManager.getFirebaseUser(reactContext);
                if(firebaseUser != null){
                    FirebaseCameraManager firebaseCameraManager = new FirebaseCameraManager();
                    firebaseCameraManager.getCameraConfigUpdates(firebaseUser);
                    FirebaseAlarmEventManager firebaseAlarmEventManager = new FirebaseAlarmEventManager();
                    firebaseAlarmEventManager.getAlarmEventUpdates(firebaseUser, reactContext);
                }
            } catch (Exception e) {
                LOGGER.e(e, "Error getting updates from firebase");
            }
        });
    }

    private Date convertStringToDate(String dateInString){
        Date formattedDate = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            formattedDate = formatter.parse(dateInString);
        } catch (Exception e) {
            LOGGER.d("error parsing smartthings expiration date");
        }
        return formattedDate;
    }

    public class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context applicationContext, Intent intent) {
            try {
                ReactApplicationContext reactApplicationContext = (ReactApplicationContext) reactContext;

                AlarmEvent alarmEvent = (AlarmEvent) intent.getSerializableExtra(AppConstants.NEW_DETECTION_EVENT);
                if (alarmEvent != null) {
                    String jsonString = gson.toJson(alarmEvent);
                    JSONObject jsonObject = new JSONObject(jsonString);
                    WritableMap alarmEventMap = ConversionUtil.convertJsonToMap(jsonObject);
                    reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(AppConstants.NEW_DETECTION_JS_EVENT, alarmEventMap);
                } else {
                    CameraConfig cameraConfig = (CameraConfig) intent.getSerializableExtra(AppConstants.STATUS_CHANGED_EVENT);
                    if (cameraConfig != null) {
                        String jsonString = gson.toJson(cameraConfig);
                        JSONObject jsonObject = new JSONObject(jsonString);
                        WritableMap camerConfigMap = ConversionUtil.convertJsonToMap(jsonObject);
                        reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(AppConstants.STATUS_CHANGED_EVENT_JS_EVENT, camerConfigMap);
                    }
                }
            } catch (Exception e) {
                LOGGER.e("Exception notifying UI about events " + e.getMessage());
            }
        }
    }

    @Override
    public String getName() {
        return "RNSmartCam";
    }
}
