package io.github.zero88.qwe.iot.connector;

import io.github.zero88.qwe.component.HasSharedData;
import io.github.zero88.qwe.iot.data.IoTEntity;
import io.github.zero88.qwe.protocol.HasProtocol;

import lombok.NonNull;

/**
 * The interface {@code RPC protocol}.
 *
 * @see HasProtocol
 * @see HasSharedData
 * @since 1.0.0
 */
public interface RpcProtocol<T extends IoTEntity> extends HasProtocol, HasSharedData {

    /**
     * Declares context that represents for the protocol entity
     *
     * @return class of protocol entity
     * @see IoTEntity
     */
    @NonNull Class<T> context();

}
