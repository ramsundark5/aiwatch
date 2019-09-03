//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.evercam.network;

import io.evercam.Vendor;
import io.evercam.network.discovery.Device;
import io.evercam.network.discovery.DiscoveredCamera;
import io.evercam.network.discovery.IpScan;
import io.evercam.network.discovery.MacAddress;
import io.evercam.network.discovery.NatMapEntry;
import io.evercam.network.discovery.NetworkInfo;
import io.evercam.network.discovery.ScanRange;
import io.evercam.network.discovery.ScanResult;
import io.evercam.network.discovery.UpnpDevice;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CustomEvercamDiscover {
    public static final int DEFAULT_FIXED_POOL = 20;
    private ArrayList<String> activeIpList = new ArrayList();
    private ArrayList<UpnpDevice> deviceList = new ArrayList();
    private ArrayList<NatMapEntry> mapEntries = new ArrayList();
    private ArrayList<DiscoveredCamera> cameraList = new ArrayList();
    private ArrayList<DiscoveredCamera> onvifDeviceList = new ArrayList();
    private ArrayList<Device> nonCameraDeviceList = new ArrayList();
    private boolean upnpDone = false;
    private boolean natDone = false;
    private int countDone = 0;
    private int queryCountDone = 0;
    private String externalIp = "";
    private boolean withDefaults = false;
    public ExecutorService pool;
    public static long NAT_TIMEOUT = 5000L;
    public static long IDENTIFICATION_TIMEOUT = 16000L;
    public static long QUERY_TIMEOUT = 12000L;
    private OnvifRunnable onvifRunnable = new OnvifRunnable() {
        public void onFinished() {
            CustomEvercamDiscover.printLogMessage("ONVIF discovery finished.");
        }

        public void onDeviceFound(DiscoveredCamera discoveredCamera) {
            CustomEvercamDiscover.printLogMessage("Found ONVIF device: " + discoveredCamera.getIP());
            discoveredCamera.setExternalIp(CustomEvercamDiscover.this.externalIp);
            CustomEvercamDiscover.this.onvifDeviceList.add(discoveredCamera);
        }
    };
    private UpnpRunnable upnpRunnable = new UpnpRunnable() {
        public void onFinished(ArrayList<UpnpDevice> upnpDeviceList) {
            EvercamDiscover.printLogMessage("UPnP discovery finished.");
            if (upnpDeviceList != null) {
                CustomEvercamDiscover.this.deviceList = upnpDeviceList;
            }

            CustomEvercamDiscover.this.upnpDone = true;
        }

        public void onDeviceFound(UpnpDevice upnpDevice) {
            EvercamDiscover.printLogMessage("Found UPnP device: " + upnpDevice.getIp());
        }
    };

    public CustomEvercamDiscover() {
    }

    public CustomEvercamDiscover withDefaults(boolean withDefaults) {
        this.withDefaults = withDefaults;
        return this;
    }

    public DiscoveryResult discoverAllLinux(ScanRange scanRange) throws Exception {
        this.pool = Executors.newFixedThreadPool(20);
        this.externalIp = NetworkInfo.getExternalIP();
        if (!this.pool.isShutdown()) {
            this.pool.execute(this.onvifRunnable);
            printLogMessage("Discovering ONVIF devices......");
            this.pool.execute(this.upnpRunnable);
            if (scanRange.getRouterIpString().equals(NetworkInfo.getLinuxRouterIp())) {
                printLogMessage("Discovering UPnP devices......");
                this.pool.execute(new NatRunnable(scanRange.getRouterIpString()) {
                    public void onFinished(ArrayList<NatMapEntry> mapEntries) {
                        CustomEvercamDiscover.printLogMessage("NAT discovery finished.");
                        if (mapEntries != null) {
                            CustomEvercamDiscover.this.mapEntries = mapEntries;
                        }

                        CustomEvercamDiscover.this.natDone = true;
                    }
                });
            }

            printLogMessage("Discovering NAT table......");
        }

        IpScan ipScan = new IpScan(new ScanResult() {
            public void onActiveIp(String ip) {
                CustomEvercamDiscover.printLogMessage("Active IP: " + ip);
                CustomEvercamDiscover.this.activeIpList.add(ip);
            }

            public void onIpScanned(String ip) {
            }
        });
        ipScan.scanAll(scanRange);

        for(long natWaitingTime = 0L; !this.upnpDone || !this.natDone; natWaitingTime += 2000L) {
            if (natWaitingTime >= NAT_TIMEOUT) {
                printLogMessage("UPnP & NAT discovery timeout.");
                break;
            }

            printLogMessage("Waiting for UPnP & NAT discovery...");
            Thread.sleep(2000L);
        }

        printLogMessage("Identifying cameras......");

        for(int index = 0; index < this.activeIpList.size(); ++index) {
            if (!this.pool.isShutdown()) {
                this.pool.execute(new CustomIdentifyCameraRunnable((String)this.activeIpList.get(index)) {
                    public void onCameraFound(DiscoveredCamera discoveredCamera, Vendor vendor) {
                        discoveredCamera.setExternalIp(CustomEvercamDiscover.this.externalIp);
                        discoveredCamera = CustomEvercamDiscover.mergeUpnpDevicesToCamera(discoveredCamera, CustomEvercamDiscover.this.deviceList);
                        discoveredCamera = CustomEvercamDiscover.mergeNatTableToCamera(discoveredCamera, CustomEvercamDiscover.this.mapEntries);
                        synchronized(CustomEvercamDiscover.this.cameraList) {
                            CustomEvercamDiscover.this.cameraList.add(discoveredCamera);
                        }
                    }

                    public void onFinished() {
                        CustomEvercamDiscover var10000 = CustomEvercamDiscover.this;
                        var10000.countDone = var10000.countDone + 1;
                    }

                    public void onNonCameraDeviceFound(Device device) {
                        device.setExternalIp(CustomEvercamDiscover.this.externalIp);
                        synchronized(CustomEvercamDiscover.this.nonCameraDeviceList) {
                            CustomEvercamDiscover.this.nonCameraDeviceList.add(device);
                        }
                    }
                });
            }
        }

        for(long identificationWaitingTime = 0L; this.countDone != this.activeIpList.size(); identificationWaitingTime += 4000L) {
            if (identificationWaitingTime >= IDENTIFICATION_TIMEOUT) {
                printLogMessage("Camera identification timeout.");
                break;
            }

            printLogMessage("Identifying cameras..." + this.countDone + '/' + this.activeIpList.size());
            Thread.sleep(4000L);
        }

        this.discardOnvifDeviceIfNotInScanRange(scanRange);
        this.mergeOnvifDeviceListToCameraList();
        if (!this.pool.isShutdown()) {
            Iterator var8 = this.cameraList.iterator();

            while(var8.hasNext()) {
                DiscoveredCamera discoveredCamera = (DiscoveredCamera)var8.next();
                this.pool.execute((new EvercamQueryRunnable(discoveredCamera) {
                    public void onFinished() {
                        CustomEvercamDiscover var10000 = CustomEvercamDiscover.this;
                        var10000.queryCountDone = var10000.queryCountDone + 1;
                    }
                }).withDefaults(this.withDefaults));
            }
        }

        for(long queryWaitingTime = 0L; this.queryCountDone != this.cameraList.size(); queryWaitingTime += 4000L) {
            if (queryWaitingTime >= QUERY_TIMEOUT) {
                printLogMessage("Evercam query timeout.");
                break;
            }

            printLogMessage("Retrieving camera defaults..." + this.queryCountDone + '/' + this.cameraList.size());
            Thread.sleep(4000L);
        }

        this.pool.shutdown();

        try {
            if (!this.pool.awaitTermination(3600L, TimeUnit.SECONDS)) {
                this.pool.shutdownNow();
            }
        } catch (InterruptedException var10) {
            this.pool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        mergeDuplicateCameraFromList(this.cameraList);
        fillMacAddressIfNotExist(this.cameraList);
        return new DiscoveryResult(this.cameraList, this.nonCameraDeviceList);
    }

    public static DiscoveredCamera mergeSingleUpnpDeviceToCamera(UpnpDevice upnpDevice, DiscoveredCamera discoveredCamera) {
        int port = upnpDevice.getPort();
        String model = upnpDevice.getModel();
        if (port > 0) {
            discoveredCamera.setHttp(port);
        }

        discoveredCamera.setName(upnpDevice.getFriendlyName());
        discoveredCamera.setModel(model);
        return discoveredCamera;
    }

    public static DiscoveredCamera mergeUpnpDevicesToCamera(DiscoveredCamera camera, ArrayList<UpnpDevice> upnpDeviceList) {
        try {
            if (upnpDeviceList.size() > 0) {
                Iterator var3 = upnpDeviceList.iterator();

                while(var3.hasNext()) {
                    UpnpDevice upnpDevice = (UpnpDevice)var3.next();
                    String ipFromUpnp = upnpDevice.getIp();
                    if (ipFromUpnp != null && !ipFromUpnp.isEmpty() && camera.getIP().equals(ipFromUpnp)) {
                        mergeSingleUpnpDeviceToCamera(upnpDevice, camera);
                        break;
                    }
                }
            }
        } catch (Exception var5) {
            printLogMessage("Exception while merging UPnP device: " + var5.getStackTrace().toString());
        }

        return camera;
    }

    public static DiscoveredCamera mergeNatEntryToCamera(DiscoveredCamera camera, NatMapEntry mapEntry) {
        int natInternalPort = mapEntry.getInternalPort();
        int natExternalPort = mapEntry.getExternalPort();
        if (camera.getHttp() == natInternalPort) {
            camera.setExthttp(natExternalPort);
        }

        if (camera.getRtsp() == natInternalPort) {
            camera.setExtrtsp(natExternalPort);
        }

        return camera;
    }

    public static DiscoveredCamera mergeNatTableToCamera(DiscoveredCamera camera, ArrayList<NatMapEntry> mapEntries) {
        if (mapEntries != null && mapEntries.size() > 0) {
            Iterator var3 = mapEntries.iterator();

            while(var3.hasNext()) {
                NatMapEntry mapEntry = (NatMapEntry)var3.next();
                String natIp = mapEntry.getIpAddress();
                if (camera.getIP().equals(natIp)) {
                    mergeNatEntryToCamera(camera, mapEntry);
                }
            }
        }

        return camera;
    }

    public static void mergeDuplicateCameraFromList(ArrayList<DiscoveredCamera> cameraList) {
        boolean duplicate = false;

        label40:
        do {
            duplicate = false;
            int listSize = cameraList.size();

            for(int index1 = 0; index1 < listSize; ++index1) {
                DiscoveredCamera camera1 = (DiscoveredCamera)cameraList.get(index1);
                String ip1 = camera1.getIP();
                String mac1 = camera1.getMAC();

                for(int index2 = index1 + 1; index2 < listSize; ++index2) {
                    DiscoveredCamera camera2 = (DiscoveredCamera)cameraList.get(index2);
                    String ip2 = camera2.getIP();
                    String mac2 = camera2.getMAC();
                    if (ip1.equals(ip2)) {
                        duplicate = true;
                        camera1.merge(camera2);
                        cameraList.remove(index2);
                        continue label40;
                    }

                    if (!mac1.isEmpty() && !mac2.isEmpty() && mac1.equals(mac2) && camera1.isduplicateWith(camera2)) {
                        duplicate = true;
                        camera1.setNotes("Duplicate MAC address with another IP address: " + ip2);
                        cameraList.remove(camera2);
                        continue label40;
                    }
                }
            }
        } while(duplicate);

    }

    public static void fillMacAddressIfNotExist(ArrayList<DiscoveredCamera> cameraList) {
        Iterator var2 = cameraList.iterator();

        while(var2.hasNext()) {
            DiscoveredCamera camera = (DiscoveredCamera)var2.next();
            if (!camera.hasMac()) {
                camera.setMAC(MacAddress.getByIpLinux(camera.getIP()));
            }
        }

    }

    private void discardOnvifDeviceIfNotInScanRange(ScanRange scanRange) {
        ArrayList<DiscoveredCamera> clonedList = (ArrayList)this.onvifDeviceList.clone();
        if (this.onvifDeviceList.size() > 0) {
            Iterator var4 = this.onvifDeviceList.iterator();

            while(var4.hasNext()) {
                DiscoveredCamera discoveredCamera = (DiscoveredCamera)var4.next();

                try {
                    if (!scanRange.containIp(discoveredCamera.getIP())) {
                        printLogMessage("Removing ONVIF device: " + discoveredCamera.getIP());
                        clonedList.remove(discoveredCamera);
                    }
                } catch (Exception var6) {
                    if (Constants.ENABLE_LOGGING) {
                        var6.printStackTrace();
                    }
                }
            }

            this.onvifDeviceList = clonedList;
        }

    }

    private void mergeOnvifDeviceListToCameraList() {
        if (this.onvifDeviceList.size() > 0) {
            Iterator var2 = this.onvifDeviceList.iterator();

            while(var2.hasNext()) {
                DiscoveredCamera onvifCamera = (DiscoveredCamera)var2.next();
                boolean matched = false;
                if (this.cameraList.size() > 0) {
                    Iterator var5 = this.cameraList.iterator();

                    while(var5.hasNext()) {
                        DiscoveredCamera discoveredCamera = (DiscoveredCamera)var5.next();
                        if (discoveredCamera.getIP().equals(onvifCamera.getIP())) {
                            matched = true;
                            if (onvifCamera.hasModel()) {
                                discoveredCamera.setModel(onvifCamera.getModel());
                                discoveredCamera.setHttp(onvifCamera.getHttp());
                            }
                            break;
                        }
                    }
                }

                if (!matched) {
                    this.cameraList.add(onvifCamera);
                }
            }
        }

    }

    public static void printLogMessage(String message) {
        if (Constants.ENABLE_LOGGING) {
            System.out.println(message);
        }

    }
}
