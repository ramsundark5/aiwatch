package io.evercam.network;

import com.aiwatch.Logger;

import io.evercam.Vendor;
import io.evercam.network.discovery.Device;
import io.evercam.network.discovery.DiscoveredCamera;
import io.evercam.network.discovery.MacAddress;
import io.evercam.network.discovery.Port;
import io.evercam.network.discovery.PortScan;
import io.evercam.network.query.CustomPublicVendor;
import io.evercam.network.query.EvercamQuery;

import java.net.InetAddress;
import java.util.ArrayList;

public abstract class CustomIdentifyCameraRunnable implements Runnable {
    private static final Logger LOGGER = new Logger();
    private String ip;

    public CustomIdentifyCameraRunnable(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        EvercamDiscover.printLogMessage("Identifying : " + ip);
        try {
            String macAddress = MacAddress.getByIpLinux(ip);
            if (!macAddress.equals(Constants.EMPTY_MAC)) {
                Vendor vendor = EvercamQuery.getCameraVendorByMac(macAddress);
                if (vendor != null) {
                    String vendorId = vendor.getId();
                    if (!vendorId.isEmpty()) {
                        EvercamDiscover.printLogMessage(ip
                                + " is identified as a camera, vendor is: "
                                + vendorId);
                        // Then fill details discovered from IP scan
                        DiscoveredCamera camera = new DiscoveredCamera(ip);
                        camera.setMAC(macAddress);
                        camera.setVendor(vendorId);

                        // Start port scan
                        PortScan portScan = new PortScan();
                        portScan.start(ip);
                        ArrayList<Port> activePortList = portScan
                                .getActivePorts();

                        if (activePortList.size() > 0) {
                            camera = camera.mergePorts(activePortList);
                        }
                        onCameraFound(camera, vendor);
                    }
                } else {
                    Device device = new Device(ip);
                    device.setMAC(macAddress);
                    CustomPublicVendor publicVendor = CustomPublicVendor.getByMac(macAddress);
                    String canonicalHostName = (InetAddress.getByName(ip)).getCanonicalHostName();
                    String vendorStr = canonicalHostName;
                    if(publicVendor != null){
                        vendorStr = canonicalHostName + publicVendor.getCompany();
                    }
                    device.setPublicVendor(vendorStr);
                    onNonCameraDeviceFound(device);
                }
            }
        } catch (Exception e) {
            LOGGER.e(e, "Error identifying camera");
        }

        EvercamDiscover.printLogMessage("Identification finished:  " + ip);

        onFinished();
    }

    public abstract void onCameraFound(DiscoveredCamera discoveredCamera,
                                       Vendor vendor);

    public abstract void onNonCameraDeviceFound(Device device);

    public abstract void onFinished();
}
