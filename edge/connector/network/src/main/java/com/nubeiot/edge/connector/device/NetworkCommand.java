package com.nubeiot.edge.connector.device;

public interface NetworkCommand {

    /**
     * @param networkInfo NetworkInfo to set IP
     * @return Set IP Address
     */
    String configIp(NetworkInfo networkInfo);

    /**
     * Release the IP and will configure with DHCP
     */
    void configDhcp();

}
