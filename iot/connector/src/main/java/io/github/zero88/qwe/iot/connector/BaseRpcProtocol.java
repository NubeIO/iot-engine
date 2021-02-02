package io.github.zero88.qwe.iot.connector;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.iot.data.IoTEntity;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class BaseRpcProtocol<P extends IoTEntity> extends BaseService implements RpcProtocol<P> {

    protected BaseRpcProtocol(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

}
