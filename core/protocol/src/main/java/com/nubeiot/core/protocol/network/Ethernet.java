package com.nubeiot.core.protocol.network;

import com.nubeiot.core.exceptions.CommunicationProtocolException;
import com.nubeiot.core.protocol.CommunicationProtocol;

/**
 * Ethernet
 */
public interface Ethernet extends CommunicationProtocol {

    @Override
    Ethernet isReachable() throws CommunicationProtocolException;

    int getIndex();

    String getName();

    String getDisplayName();

    String getMacAddress();

    String getCidrAddress();

    String getHostAddress();

}
