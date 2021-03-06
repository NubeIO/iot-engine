package com.nubeiot.core.protocol.usb;

import com.nubeiot.core.exceptions.CommunicationProtocolException;
import com.nubeiot.core.protocol.CommunicationProtocol;

import lombok.NonNull;

/**
 * USB protocol
 */
public interface UsbProtocol extends CommunicationProtocol {

    static UsbProtocol parse(@NonNull String key) {
        return null;
    }

    @Override
    UsbProtocol isReachable() throws CommunicationProtocolException;

}
