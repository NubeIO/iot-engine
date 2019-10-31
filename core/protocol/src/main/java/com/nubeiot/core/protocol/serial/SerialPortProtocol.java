package com.nubeiot.core.protocol.serial;

import com.nubeiot.core.exceptions.CommunicationProtocolException;
import com.nubeiot.core.protocol.CommunicationProtocol;

/**
 * Serial port protocol
 */
public interface SerialPortProtocol extends CommunicationProtocol {

    @Override
    SerialPortProtocol isReachable() throws CommunicationProtocolException;

}
