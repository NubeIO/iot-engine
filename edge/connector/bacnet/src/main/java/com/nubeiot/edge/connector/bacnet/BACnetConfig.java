package com.nubeiot.edge.connector.bacnet;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;

import lombok.Getter;

@Getter
public class BACnetConfig implements IConfig {

    @Override
    public String key() {
        return NAME;
    }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

    public static final String NAME = "__bacnet__";

    private String deviceName = "NubeIO-Edge28";
    private String modelName = deviceName;
    private int deviceId = 123456;
    private long discoveryTimeout = 10000;
    private boolean allowSlave = true;
    private String localPointsApiAddress = "/edge-api/points";
    @JsonProperty(value = "__ipConfigs__")
    private List<IPConfig> ipConfigs = new ArrayList<>();
    @JsonProperty(value = "__mstpConfigs__")
    private List<MSTPConfig> mstpConfigs = new ArrayList<>();


    @Getter
    public static class BACnetNetworkConfig implements IConfig {

        private String name;

        @Override
        public String key() { return "BLANK_NETWORK"; }

        @Override
        public Class<? extends IConfig> parent() { return BACnetConfig.class; }

    }


    @Getter
    public static class IPConfig extends BACnetNetworkConfig {

        public static final String NAME = "__ip__";
        private String subnet;
        private String networkInterface;
        private int port = IpNetwork.DEFAULT_PORT;

        @Override
        public String key() { return NAME; }

    }


    @Getter
    public static class MSTPConfig extends BACnetNetworkConfig {

        public static final String NAME = "__mstp__";
        private String port;
        private int baud;
        private int parity;
        private int buffer;

        @Override
        public String key() {
            return NAME;
        }

    }

}
