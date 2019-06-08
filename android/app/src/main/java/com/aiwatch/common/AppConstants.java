package com.aiwatch.common;

public class AppConstants {

    public static final int FRAME_RATE = 5;
    public static final int TF_OD_API_INPUT_SIZE                = 300;
    //public static final long WAIT_TIME_AFTER_DETECT           = 600000; //10 mins
    public static final long WAIT_TIME_AFTER_DETECT             = 60*1000; //60 seconds
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
    public static final String UNCOMPRESSED_VIDEO_FOLDER        = "tempvideos";
    public static final String COMPRESSED_VIDEO_FOLDER          = "compressed";
    public static final String MERGED_VIDEO_FOLDER              = "videos";
    public static final String IMAGES_FOLDER                    = "images";

    public static final String PERSON_DETECTED_MESSAGE          = "Person Detected";
    public static final String ANIMAL_DETECTED_MESSAGE          = "Animal Detected";
    public static final String VEHICLE_DETECTED_MESSAGE         = "Vehicle Detected";
    public static final String OTHER_DETECTED_MESSAGE           = "Other event Detected";

    public static final String ADD_ALARM_EVENT                  = "ADD_ALARM_EVENT";
    public static final String UPDATE_ALARM_EVENT               = "UPDATE_ALARM_EVENT";
    public static final String DELETE_ALARM_EVENT               = "DELETE_ALARM_EVENT";
}
