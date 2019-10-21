package com.nubeiot.edge.connector.bacnet;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.Getter;

@Getter
public final class BACnetConfig implements IConfig {

    public static final int VENDOR_ID = 1173;
    public static final String VENDOR_NAME = "Nube iO Operations Pty Ltd";
    public static final int MIN_DEVICE_ID = 80000;
    public static final int MAX_DEVICE_ID = 90000;

    private int deviceId;
    private String modelName = "NubeIO-Edge28";
    private String deviceName;
    private long discoveryTimeout = 10;
    private TimeUnit discoveryTimeoutUnit = TimeUnit.SECONDS;
    private boolean allowSlave = true;
    private String gatewayDiscoverAddress = "";
    private String localPointsApiAddress = "/edge-api/points";
    @JsonProperty(value = PredefinedNetwork.KEY)
    private PredefinedNetwork networks = new PredefinedNetwork();

    private static int genDeviceId() {
        return (int) (Math.random() * (MAX_DEVICE_ID - MIN_DEVICE_ID + 1)) + MIN_DEVICE_ID;
    }

    @Override
    public String key() {
        return "__bacnet__";
    }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

    public int getDeviceId() {
        if (deviceId < 0 || deviceId > ObjectIdentifier.UNINITIALIZED) {
            throw new IllegalArgumentException("Illegal device id: " + deviceId);
        }
        deviceId = deviceId == 0 ? genDeviceId() : deviceId;
        return deviceId;
    }

    public String getDeviceName() {
        deviceName = Strings.isBlank(deviceName) ? modelName + "-" + deviceId : deviceName;
        return deviceName;
    }

    public PredefinedNetwork getNetworks() {
        return (PredefinedNetwork) networks.copy();
    }

    public static final class PredefinedNetwork extends HashMap<String, JsonObject> implements IConfig {

        static final String KEY = "__networks__";

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public Class<? extends IConfig> parent() {
            return BACnetConfig.class;
        }

        public List<BACnetNetwork> toNetworks() {
            return entrySet().stream()
                             .filter(entry -> Objects.nonNull(entry.getValue()) && Strings.isNotBlank(entry.getKey()))
                             .map(entry -> entry.getValue().copy().put("name", entry.getKey()))
                             .map(BACnetNetwork::factory)
                             .filter(Objects::nonNull)
                             .collect(Collectors.toList());
        }

    }

}
