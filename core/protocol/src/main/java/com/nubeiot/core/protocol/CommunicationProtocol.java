package com.nubeiot.core.protocol;

import com.nubeiot.core.dto.EnumType;
import com.nubeiot.core.exceptions.CommunicationProtocolException;

import lombok.NonNull;

/**
 * Represents communication protocol information could be discovered and realized by computer
 */
public interface CommunicationProtocol extends EnumType {

    /**
     * Split character is used in separate part in identifier
     */
    String SPLIT_CHAR = "-";

    /**
     * Validate current communication protocol is still reach by machine/computer
     * <p>
     * It should be call in {@code runtime} process
     *
     * @return a reference to this, so the API can be used fluently
     * @throws CommunicationProtocolException exception if unreachable
     */
    @NonNull CommunicationProtocol isReachable() throws CommunicationProtocolException;

    /**
     * Unique value to identify protocol, able to use as cache key and able to deserialize from argument.
     * <p>
     * It must be in format {@code type-...}
     *
     * @return communication protocol identifier
     * @see #type()
     * @see #SPLIT_CHAR
     */
    @NonNull String identifier();

}
