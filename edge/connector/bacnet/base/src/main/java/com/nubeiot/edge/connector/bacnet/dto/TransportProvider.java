package com.nubeiot.edge.connector.bacnet.dto;

import java.util.function.Supplier;

import com.nubeiot.core.exceptions.CommunicationProtocolException;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.nubeiot.core.protocol.serial.SerialPortProtocol;
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

    static TransportProvider byProtocol(@NonNull CommunicationProtocol protocol) {
        if (protocol instanceof UdpProtocol) {
            return new TransportIP((UdpProtocol) protocol);
        }
        if (protocol instanceof SerialPortProtocol) {
            return new TransportMstp((SerialPortProtocol) protocol);
        }
        throw new IllegalArgumentException("Does not support BACNet protocol type " + protocol.type());
    }

    /**
     * Get {@code protocol} is holden by transport provider
     *
     * @return current {@code protocol}
     */
    CommunicationProtocol protocol();

    /**
     * Get {@code transport} by {@link #protocol()} computation
     *
     * @return BACnet transport
     * @throws CommunicationProtocolException if {@code protocol} is unreachable
     */
    @Override
    Transport get();

}
