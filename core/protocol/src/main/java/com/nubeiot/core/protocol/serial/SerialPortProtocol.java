package com.nubeiot.core.protocol.serial;

import java.util.ArrayList;
import java.util.List;

import com.nubeiot.core.exceptions.CommunicationProtocolException;
import com.nubeiot.core.protocol.CommunicationProtocol;

import lombok.NonNull;

/**
 * Serial port protocol
 */
public interface SerialPortProtocol extends CommunicationProtocol {

    static List<SerialPortProtocol> getActivePorts() {
        return new ArrayList<>();
    }

    static SerialPortProtocol parse(@NonNull String key) {
        return null;
    }

    @Override
    default @NonNull String type() {
        return "serial";
    }

    @Override
    SerialPortProtocol isReachable() throws CommunicationProtocolException;

}
