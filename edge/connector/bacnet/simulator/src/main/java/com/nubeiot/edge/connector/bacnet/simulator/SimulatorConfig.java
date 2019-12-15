package com.nubeiot.edge.connector.bacnet.simulator;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bacnet.AbstractBACnetConfig;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;

final class SimulatorConfig extends AbstractBACnetConfig {

    @JsonProperty(value = PredefinedNetwork.KEY)
    private PredefinedNetwork networks = new PredefinedNetwork();

    @Override
    public boolean isAllowSlave() {
        return false;
    }

    @Override
    protected int maxDeviceId() {
        return 91000;
    }

    @Override
    protected int minDeviceId() {
        return MAX_DEVICE_ID + 1;
    }

    PredefinedNetwork getNetworks() {
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
            return AbstractBACnetConfig.class;
        }

        List<BACnetNetwork> toNetworks() {
            return entrySet().stream()
                             .filter(entry -> Objects.nonNull(entry.getValue()) && Strings.isNotBlank(entry.getKey()))
                             .map(entry -> entry.getValue().copy().put("label", entry.getKey()))
                             .map(data -> Functions.getIfThrow(() -> BACnetNetwork.factory(data)))
                             .filter(Optional::isPresent)
                             .map(Optional::get)
                             .collect(Collectors.toList());
        }

    }

}
