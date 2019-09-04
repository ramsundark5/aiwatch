package com.aiwatch.media;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.aiwatch.Logger;
import com.aiwatch.common.NetInfo;

import java.util.List;

import io.evercam.network.CustomEvercamDiscover;
import io.evercam.network.DiscoveryResult;
import io.evercam.network.discovery.Device;
import io.evercam.network.discovery.DiscoveredCamera;
import io.evercam.network.discovery.ScanRange;

public class DeviceDiscovery {
    private static final Logger LOGGER = new Logger();
    private CustomEvercamDiscover evercamDiscover = new CustomEvercamDiscover();

    public void discover(Context context){
        WifiManager.MulticastLock lock = null;
        try {
            lock = lockMulticast(context);
            NetInfo netInfo = new NetInfo(context);
            //ScanRange scanRange = new ScanRange("192.168.1.1", "255.255.255.0");
            ScanRange scanRange = new ScanRange(netInfo.getGatewayIp(), netInfo.getNetmaskIp());
            DiscoveryResult discoveryResult = evercamDiscover.discoverAllLinux(scanRange);
            List<DiscoveredCamera> discoveredCameraList = discoveryResult.getCameras();
            List<Device> devices = discoveryResult.getOtherDevices();
            LOGGER.d("completed device discovery");
        } catch (Exception e) {
            LOGGER.e(e, "Exception discovering devices");
        } finally {
            if(lock != null){
                lock.release();
            }
        }
    }

    private WifiManager.MulticastLock lockMulticast(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null)
            return null;

        WifiManager.MulticastLock lock = wifi.createMulticastLock("ONVIF");
        lock.acquire();
        return lock;
    }
}
