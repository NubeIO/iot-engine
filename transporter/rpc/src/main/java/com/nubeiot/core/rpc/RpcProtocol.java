package com.nubeiot.core.rpc;

import com.nubeiot.iotdata.HasProtocol;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * The interface {@code RPC protocol}.
 *
 * @since 1.0.0
 */
public interface RpcProtocol<T extends IoTEntity> extends HasProtocol {

    /**
     * Declares context that represents for the entity model
     *
     * @return entity metadata
     * @see IoTEntity
     */
    @NonNull Class<T> context();

}
