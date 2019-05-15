package com.aiwatch.common;

public class AppConstants {

    public static final int FRAME_RATE = 5;
    public static final int FRAME_GRAB_WIDTH = 640;
    public static final int FRAME_GRAB_HEIGHT = 480;
    public static final int TF_OD_API_INPUT_SIZE = 300;
    //public static final long WAIT_TIME_AFTER_DETECT = 600000; //10 mins
    public static final long WAIT_TIME_AFTER_DETECT = 60*1000; //60 seconds
    public static final String START_MONITORING = "start_monitoring";
    public static final String STOP_MONITORING = "stop_monitoring";
    public static final String SAVE_CAMERA = "save_camera";
    public static final String REMOVE_CAMERA = "remove_camera";
    public static final String CAMERA_CONFIG_EXTRA = "camera_config_extra";
    public static final String CAMERA_CONFIG_ID_EXTRA = "camera_config_id_extra";
    public static final String ACTION_EXTRA = "action_extra";
    public static final String AIWATCH_EVENT_INTENT = "AIWATCH_EVENT_INTENT";
    public static final String NEW_DETECTION_EVENT = "NEW_DETECTION_EVENT";
    public static final String NEW_DETECTION_JS_EVENT = "NEW_DETECTION_JS_EVENT";
}
