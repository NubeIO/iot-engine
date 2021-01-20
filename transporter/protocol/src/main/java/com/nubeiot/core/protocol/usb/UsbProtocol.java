package com.nubeiot.core.protocol.usb;

import io.github.zero88.msa.bp.exceptions.CommunicationProtocolException;

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
