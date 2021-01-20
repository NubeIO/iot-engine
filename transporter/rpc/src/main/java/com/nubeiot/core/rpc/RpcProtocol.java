package com.nubeiot.core.rpc;

import io.github.zero88.msa.bp.dto.JsonData;

import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

/**
 * The interface {@code RPC protocol}.
 *
 * @since 1.0.0
 */
public interface RpcProtocol<T extends JsonData> {

    /**
     * Declares context that represents for the entity model
     *
     * @return entity metadata
     * @see JsonData
     */
    @NonNull T context();

    /**
     * Defines service protocol
     *
     * @return protocol protocol
     * @see Protocol
     * @since 1.0.0
     */
    @NonNull Protocol protocol();

}
