package com.nubeiot.edge.connector.bacnet;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;

import lombok.Getter;

@Getter
public final class BACnetConfig implements IConfig {

    public static final int VENDOR_ID = 1173;
    public static final String VENDOR_NAME = "Nube iO Operations Pty Ltd";

    private String deviceName = "NubeIO-Edge28";
    private String modelName = deviceName;
    private int deviceId = 4321;
    private long discoveryTimeout = 10000;
    private boolean allowSlave = true;
    private String localPointsApiAddress = "/edge-api/points";
    @JsonProperty(value = PredefinedNetwork.KEY)
    private PredefinedNetwork networks = new PredefinedNetwork();

    @Override
    public String key() {
        return "__bacnet__";
    }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

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
