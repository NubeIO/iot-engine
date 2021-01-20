package com.nubeiot.edge.connector.bacnet.service;

import org.slf4j.Logger;

import io.github.zero88.qwe.exceptions.CarlException;
import io.github.zero88.qwe.exceptions.converter.CarlExceptionConverter;

import com.nubeiot.core.rpc.RpcProtocol;
import com.nubeiot.edge.connector.bacnet.entity.BACnetProtocol;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents for BACnet RPC protocol that interacts with {@code external services}
 */
public interface BACnetRpcProtocol<P extends IoTEntity> extends RpcProtocol<P>, BACnetProtocol {

    static void sneakyThrowable(@NonNull Logger logger, @NonNull Throwable t, boolean isMaster) {
        final CarlException error = CarlExceptionConverter.friendly(t);
        if (isMaster) {
            throw error;
        }
        logger.debug("Failed to connect to remote service", error);
    }

}
