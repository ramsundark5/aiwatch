package io.evercam.network.discovery;

import io.evercam.network.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;


public class CustomNetworkInfo {

    public static ArrayList<String> getNetworkInterfaceNames() {
        Enumeration<NetworkInterface> networkInterfaces = null;
        ArrayList<String> interfaceNameArrayList = new ArrayList<>();
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
            for (Enumeration<NetworkInterface> networkInterfaceEnum = networkInterfaces; networkInterfaces
                    .hasMoreElements(); ) {
                NetworkInterface networkInterface = networkInterfaceEnum
                        .nextElement();
                for (Enumeration<InetAddress> nis = networkInterface
                        .getInetAddresses(); nis.hasMoreElements(); ) {
                    InetAddress thisInetAddress = nis.nextElement();
                    if (!thisInetAddress.isLoopbackAddress()) {
                        if (thisInetAddress instanceof Inet6Address) {
                            continue;
                        } else {
                            interfaceNameArrayList.add(networkInterface
                                    .getName());
                        }
                    }
                }
            }
        } catch (SocketException e) {
            if (Constants.ENABLE_LOGGING) {
                e.printStackTrace();
            }
        }
        return interfaceNameArrayList;
    }

    /**
     * Return network interface by interface name. Return null if no interface
     * matches the given name.
     */
    public static NetworkInterface getNetworkInterfaceByName(
            String interfaceName) {
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
            for (Enumeration<NetworkInterface> networkInterfaceEnum = networkInterfaces; networkInterfaces
                    .hasMoreElements(); ) {
                NetworkInterface networkInterface = networkInterfaceEnum
                        .nextElement();
                for (Enumeration<InetAddress> nis = networkInterface
                        .getInetAddresses(); nis.hasMoreElements(); ) {
                    InetAddress thisInetAddress = nis.nextElement();
                    if (!thisInetAddress.isLoopbackAddress()) {
                        if (thisInetAddress instanceof Inet6Address) {
                            continue;
                        } else {
                            if (networkInterface.getName()
                                    .equals(interfaceName)) {
                                return networkInterface;
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            if (Constants.ENABLE_LOGGING) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Return network interface by host IP address. Return null if no interface
     * matches the given IP
     */
    public static NetworkInterface getNetworkInterfaceByIp(String ipAddress) {
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
            for (Enumeration<NetworkInterface> networkInterfaceEnum = networkInterfaces; networkInterfaces
                    .hasMoreElements(); ) {
                NetworkInterface networkInterface = networkInterfaceEnum
                        .nextElement();
                for (Enumeration<InetAddress> nis = networkInterface
                        .getInetAddresses(); nis.hasMoreElements(); ) {
                    InetAddress thisInetAddress = nis.nextElement();
                    if (!thisInetAddress.isLoopbackAddress()) {
                        if (thisInetAddress instanceof Inet6Address) {
                            continue;
                        } else {
                            if (thisInetAddress.getHostAddress().equals(
                                    ipAddress)) {
                                return networkInterface;
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            if (Constants.ENABLE_LOGGING) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Return the network prefix length. Return 0 if no CIDR detected. FIXME:
     * This method may return -1, which means it may not be the right approach
     */
    public static int getCidrFromInterface(NetworkInterface networkInterface)
            throws IOException {
        for (InterfaceAddress address : networkInterface
                .getInterfaceAddresses()) {
            InetAddress inetAddress = address.getAddress();
            if (!inetAddress.isLoopbackAddress()) {
                if (inetAddress instanceof Inet4Address) {
                    return address.getNetworkPrefixLength();
                }
            }
        }
        return 0;
    }

    /**
     * Return the valid ipv4 address for the given network interface. Return
     * empty string if IP address available.
     */
    public static String getIpFromInterface(NetworkInterface networkInterface)
            throws IOException {
        for (InterfaceAddress address : networkInterface
                .getInterfaceAddresses()) {
            InetAddress inetAddress = address.getAddress();
            if (!inetAddress.isLoopbackAddress()) {
                if (inetAddress instanceof Inet4Address) {
                    return inetAddress.getHostAddress();
                }
            }
        }
        return "";
    }

    public static String getExternalIP() {
        String extIP = "";
        BufferedReader in = null;
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            extIP = in.readLine();
        } catch (IOException e) {
            if (Constants.ENABLE_LOGGING) {
                e.printStackTrace();
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return (extIP == "" ? "" : extIP.replace("\n", ""));
    }

    /**
     * Run command 'netstat -rn' and abstract router IP
     * <p>
     * Example of Kernel IP routing table Destination Gateway Genmask Flags MSS
     * Window irtt Iface 0.0.0.0 192.168.1.1 0.0.0.0 UG 0 0 0 eth0 192.168.1.0
     * 0.0.0.0 255.255.255.0 U 0 0 0 eth0
     * <p>
     * Return router IP in Linux system. Return empty string if exception
     * occurred.
     */
    // FIXME: netstat -rn doesn't work when Internet is not connected
    public static String getRouterIp() {
        try {
            Process result = Runtime.getRuntime().exec("netstat -rn");

            BufferedReader output = new BufferedReader(new InputStreamReader(
                    result.getInputStream()));

            String line = clean(output.readLine());
            while (line != null) {
                if (line.startsWith("0.0.0.0")) {
                    break;
                }
                line = clean(output.readLine());
            }

            StringTokenizer st = new StringTokenizer(line);
            st.nextToken();
            return st.nextToken();
        } catch (Exception e) {
            if (Constants.ENABLE_LOGGING) {
                e.printStackTrace();
            }
            return "";
        }
    }

    /**
     * Run command 'netstat -rn' and abstract subnet mask
     * <p>
     * Example of Kernel IP routing table Destination Gateway Genmask Flags MSS
     * Window irtt Iface 0.0.0.0 192.168.1.1 0.0.0.0 UG 0 0 0 eth0 192.168.1.0
     * 0.0.0.0 255.255.255.0 U 0 0 0 eth0
     * <p>
     * Return subnet mask in Linux system. Return empty string if exception
     * occurred.
     */
    public static String getSubnetMask() {
        try {
            Process result = Runtime.getRuntime().exec("netstat -rn");

            BufferedReader output = new BufferedReader(new InputStreamReader(
                    result.getInputStream()));

            String line = clean(output.readLine());
            while (line != null) {
                if (!line.isEmpty()) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    String netmask = "";
                    String gateway = "";
                    if(st.hasMoreElements()) {
                        gateway = st.nextToken();
                    }
                    if(st.hasMoreElements()) {
                        netmask = st.nextToken();
                    }
                    if (gateway.equals("0.0.0.0")) {
                        return netmask;
                    }
                }
                line = clean(output.readLine());
            }
        } catch (Exception e) {
            if (Constants.ENABLE_LOGGING) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static String clean(String s) {
        return s.replaceAll("\t", " ")      // tab to space
                .replaceAll("\\s+", " ")    // remove multiple spaces
                .trim();                           // trim spaces
    }
}