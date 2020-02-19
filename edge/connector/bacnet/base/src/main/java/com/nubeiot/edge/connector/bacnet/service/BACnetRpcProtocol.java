package com.nubeiot.edge.connector.bacnet.service;

import io.vertx.core.logging.Logger;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.nubeiot.edge.module.datapoint.rpc.RpcProtocol;
import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

/**
 * Represents for BACnet RPC protocol that interacts with {@code data-point services}
 */
public interface BACnetRpcProtocol extends RpcProtocol {

    static void sneakyThrowable(@NonNull Logger logger, @NonNull Throwable t, boolean isMaster) {
        final NubeException error = NubeExceptionConverter.friendly(t);
        if (isMaster) {
            throw error;
        }
        logger.warn("Failed to connect to remote service", error);
    }

    @Override
    default @NonNull Protocol protocol() {
        return Protocol.BACNET;
    }

}
