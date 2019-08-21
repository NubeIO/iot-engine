package com.nubeiot.edge.connector.device;

class NetworkManager {

    private static String OS = System.getProperty("os.name").toLowerCase();

    static NetworkCommand findNetworkCommand() {
        if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
            return new UnixNetworkCommand();
        } else {
            throw new IllegalArgumentException("Currently this functionality is limited to the Unix OS family only.");
        }
    }

}
