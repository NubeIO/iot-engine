package com.nubeiot.edge.connector.bacnet;

import java.util.function.Supplier;

import com.nubeiot.edge.connector.bacnet.dto.BACnetIP;
import com.nubeiot.edge.connector.bacnet.dto.BACnetMSTP;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.serotonin.bacnet4j.transport.Transport;

import lombok.NonNull;

public interface TransportProvider extends Supplier<Transport> {

    static TransportProvider byConfig(@NonNull BACnetNetwork config) {
        if (config instanceof BACnetIP) {
            return TransportIP.byConfig((BACnetIP) config);
        }
        if (config instanceof BACnetMSTP) {
            return TransportMstp.byConfig((BACnetMSTP) config);
        }
        throw new IllegalArgumentException(
            "Does not support BACNet network config type " + config.getClass().getName());
    }

    BACnetNetwork config();

}
