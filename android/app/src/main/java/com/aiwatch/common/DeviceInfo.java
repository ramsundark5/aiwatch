package com.aiwatch.common;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DeviceInfo {


    public DeviceType getDeviceType(Context context) {
        // Detect TVs via ui mode (Android TVs) or system features (Fire TV).
        if (context.getPackageManager().hasSystemFeature("amazon.hardware.fire_tv")) {
            return DeviceType.TV;
        }

        UiModeManager uiManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiManager != null && uiManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            return DeviceType.TV;
        }

        DeviceType deviceTypeFromConfig = getDeviceTypeFromResourceConfiguration(context);

        if (deviceTypeFromConfig != null && deviceTypeFromConfig != DeviceType.UNKNOWN) {
            return deviceTypeFromConfig;
        }

        return  getDeviceTypeFromPhysicalSize(context);
    }

    // Use `smallestScreenWidthDp` to determine the screen size
    // https://android-developers.googleblog.com/2011/07/new-tools-for-managing-screen-sizes.html
    private DeviceType getDeviceTypeFromResourceConfiguration(Context context) {
        int smallestScreenWidthDp = context.getResources().getConfiguration().smallestScreenWidthDp;

        DeviceType deviceType = DeviceType.HANDSET;
        if (smallestScreenWidthDp == Configuration.SMALLEST_SCREEN_WIDTH_DP_UNDEFINED) {
            deviceType =  DeviceType.UNKNOWN;
        }

        if(smallestScreenWidthDp >= 600 && smallestScreenWidthDp <=800){
            deviceType =  DeviceType.SMALLTABLET;
        }
        if(smallestScreenWidthDp > 800){
            deviceType = DeviceType.LARGETABLET;
        }
        return deviceType;
    }

    private DeviceType getDeviceTypeFromPhysicalSize(Context context) {
        // Find the current window manager, if none is found we can't measure the device physical size.
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (windowManager == null) {
            return DeviceType.UNKNOWN;
        }

        // Get display metrics to see if we can differentiate handsets and tablets.
        // NOTE: for API level 16 the metrics will exclude window decor.
        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
        } else {
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }

        // Calculate physical size.
        double widthInches = metrics.widthPixels / (double) metrics.xdpi;
        double heightInches = metrics.heightPixels / (double) metrics.ydpi;
        double diagonalSizeInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));

        if (diagonalSizeInches >= 3.0 && diagonalSizeInches <= 6.9) {
            // Devices in a sane range for phones are considered to be Handsets.
            return DeviceType.HANDSET;
        } else if (diagonalSizeInches > 6.9 && diagonalSizeInches <= 8.2) {
            // Devices larger than handset and in a sane range for tablets are tablets.
            return DeviceType.SMALLTABLET;
        } else if (diagonalSizeInches > 8.2 && diagonalSizeInches <= 14) {
            // Devices larger than handset and in a sane range for tablets are tablets.
            return DeviceType.LARGETABLET;
        } else {
            // Otherwise, we don't know what device type we're on/
            return DeviceType.UNKNOWN;
        }
    }
}
