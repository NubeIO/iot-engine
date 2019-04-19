package com.nubeiot.edge.connector.bacnet;

import java.util.function.Supplier;

import com.nubeiot.edge.connector.bacnet.BACnetConfig.BACnetNetworkConfig;
import com.nubeiot.edge.connector.bacnet.BACnetConfig.IPConfig;
import com.nubeiot.edge.connector.bacnet.BACnetConfig.MSTPConfig;
import com.serotonin.bacnet4j.transport.Transport;

import lombok.NonNull;

public interface TransportProvider extends Supplier<Transport> {

    static TransportProvider byConfig(@NonNull BACnetNetworkConfig config) {
        if (config instanceof IPConfig) {
            return TransportIP.byConfig((IPConfig) config);
        }
        if (config instanceof MSTPConfig) {
            return TransportMstp.byConfig((MSTPConfig) config);
        }
        throw new IllegalArgumentException(
            "Does not support BACNet network config type " + config.getClass().getName());
    }

}
