package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.exceptions.CarlException;
import io.github.zero88.qwe.exceptions.converter.CarlExceptionConverter;
import io.vertx.core.logging.Logger;

import com.nubeiot.core.rpc.RpcProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

/**
 * Represents for BACnet RPC protocol that interacts with {@code data-point services}
 */
public interface BACnetRpcProtocol<P extends JsonData> extends RpcProtocol<P> {

    static void sneakyThrowable(@NonNull Logger logger, @NonNull Throwable t, boolean isMaster) {
        final CarlException error = CarlExceptionConverter.friendly(t);
        if (isMaster) {
            throw error;
        }
        logger.debug("Failed to connect to remote service", error);
    }

    @Override
    default @NonNull Protocol protocol() {
        return BACnetDevice.BACNET;
    }

}
