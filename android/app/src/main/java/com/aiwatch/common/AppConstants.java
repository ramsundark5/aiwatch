package com.aiwatch.common;

public class AppConstants {

    public static final int FRAME_RATE = 5;
    public static final int TF_OD_API_INPUT_SIZE                = 300;

    //public static final long WAIT_TIME_AFTER_DETECT           = 600000; //10 mins
    public static final long WAIT_TIME_AFTER_DETECT             = 5 * 60; //5 mins
    public static final String START_MONITORING                 = "start_monitoring";
    public static final String STOP_MONITORING                  = "stop_monitoring";
    public static final String CONNECT_CAMERA                   = "CONNECT_CAMERA";
    public static final String DISCONNECT_CAMERA                = "DISCONNECT_CAMERA";
    public static final String SAVE_CAMERA                      = "save_camera";
    public static final String REMOVE_CAMERA                    = "remove_camera";
    public static final String CAMERA_CONFIG_EXTRA              = "camera_config_extra";
    public static final String CAMERA_CONFIG_ID_EXTRA           = "camera_config_id_extra";
    public static final String ACTION_EXTRA                     = "action_extra";

    public static final String AIWATCH_EVENT_INTENT             = "AIWATCH_EVENT_INTENT";
    public static final String NEW_DETECTION_EVENT              = "NEW_DETECTION_EVENT";
    public static final String NEW_DETECTION_JS_EVENT           = "NEW_DETECTION_JS_EVENT";
    public static final String STATUS_CHANGED_EVENT             = "STATUS_CHANGED_EVENT";
    public static final String STATUS_CHANGED_EVENT_JS_EVENT    = "STATUS_CHANGED_EVENT_JS_EVENT";

    public static final String TEMP_VIDEO_FOLDER                = "aiwatch_tempvideos";
    public static final String EVENT_VIDEO_FOLDER               = "aiwatch_eventvideos";
    public static final String TEMP_IMAGES_FOLDER               = "aiwatch_tempimages";
    public static final String EVENT_IMAGES_FOLDER              = "aiwatch_eventimages";
    public static final String CVR_VIDEO_FOLDER                 = "aiwatch_cvr";

    public static final String PERSON_DETECTED_MESSAGE          = "Person detected";
    public static final String ANIMAL_DETECTED_MESSAGE          = "Animal detected";
    public static final String VEHICLE_DETECTED_MESSAGE         = "Vehicle detected";
    public static final String OTHER_DETECTED_MESSAGE           = "Other event detected";

    public static final String ADD_ALARM_EVENT                  = "ADD_ALARM_EVENT";
    public static final String UPDATE_ALARM_EVENT               = "UPDATE_ALARM_EVENT";
    public static final String DELETE_ALARM_EVENT               = "DELETE_ALARM_EVENT";

    public static final long FFMPEG_COMMAND_TIMEOUT             = 100; //in seconds
    public static final int PRE_RECORDING_BUFFER                = 10; //in seconds
    public static final int CVR_RECORDING_DURATION              = 20 * 60; //in seconds - 20 mins

    public static final String APP_SHARED_PREFERENCE            = "AIWATCH_SHARED_PREFERENCE";
    public static final String STOP_MONITORING_REQUESTED        = "STOP_MONITORING_REQUESTED";
    public static final String CAMERA_MONITOR_STATUS_PREFIX     = "MONITORING_CAMERA_STATUS_";

    public static final String GOOOGLE_API_CLIENT_ID            = "119466713568-8eiocl6rns75ab9sdno2r60psa03jdfk";

    public static final String DEVICE_DISCOVERY_PROGRESS_JS_EVENT         = "DEVICE_DISCOVERY_PROGRESS_JS_EVENT";
    public static final String DEVICE_DISCOVERY_COMPLETED_JS_EVENT        = "DEVICE_DISCOVERY_COMPLETED_JS_EVENT";

}
