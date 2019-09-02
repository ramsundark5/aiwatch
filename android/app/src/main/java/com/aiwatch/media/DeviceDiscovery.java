package com.aiwatch.media;

import com.aiwatch.Logger;

import io.evercam.network.DiscoveryResult;
import io.evercam.network.EvercamDiscover;
import io.evercam.network.discovery.ScanRange;


public class DeviceDiscovery {
    private static final Logger LOGGER = new Logger();
    public void discover(){
        EvercamDiscover evercamDiscover = new EvercamDiscover()
                .withDefaults(true); //Include camera defaults or not

        try{
            //Discover all cameras
            ScanRange scanRange = new ScanRange("192.168.1.1", //router IP
                    "255.255.255.0"); //subnet mask
            DiscoveryResult cameraList = evercamDiscover.discoverAllLinux(scanRange);
            LOGGER.d("discovered cameras "+cameraList.getCameras().size());
        }catch (Exception e){
            LOGGER.e(e, "Error discovering network devices");
        }
    }
}
