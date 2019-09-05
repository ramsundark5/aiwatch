package com.aiwatch.media;

import android.content.Context;

import com.aiwatch.Logger;
import com.aiwatch.common.AppConstants;
import com.aiwatch.common.NetInfo;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import io.evercam.network.CustomEvercamDiscover;
import io.evercam.network.DiscoveryResult;
import io.evercam.network.discovery.ScanRange;

public class DeviceDiscovery {
    private static final Logger LOGGER = new Logger();

    public DiscoveryResult discover(Context context){
        DiscoveryResult discoveryResult = null;
        try {
            ReactContext reactContext = (ReactContext) context;
            CustomEvercamDiscover evercamDiscover = new CustomEvercamDiscover(reactContext);
            NetInfo netInfo = new NetInfo(context);
            //ScanRange scanRange = new ScanRange("192.168.1.1", "255.255.255.0");
            ScanRange scanRange = new ScanRange(netInfo.getGatewayIp(), netInfo.getNetmaskIp());
            discoveryResult = evercamDiscover.discoverAllLinux(scanRange);
            LOGGER.d("completed device discovery");
        } catch (Exception e) {
            LOGGER.e(e, "Exception discovering devices");
        }
        return discoveryResult;
    }
}
