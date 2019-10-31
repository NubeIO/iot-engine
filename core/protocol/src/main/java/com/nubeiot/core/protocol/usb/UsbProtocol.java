package com.nubeiot.core.protocol.usb;

import com.nubeiot.core.exceptions.CommunicationProtocolException;
import com.nubeiot.core.protocol.CommunicationProtocol;

/**
 * USB protocol
 */
public interface UsbProtocol extends CommunicationProtocol {

    @Override
    UsbProtocol isReachable() throws CommunicationProtocolException;

}
