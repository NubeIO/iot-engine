package com.nubeiot.edge.module.datapoint.rpc;

import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

/**
 * The interface {@code RPC protocol}.
 *
 * @since 1.0.0
 */
public interface RpcProtocol {

    /**
     * Declares context that represents for the entity model
     *
     * @return entity metadata
     * @see EntityMetadata
     */
    @NonNull EntityMetadata context();

    /**
     * Defines service protocol
     *
     * @return protocol protocol
     * @see Protocol
     * @since 1.0.0
     */
    @NonNull Protocol protocol();

}
