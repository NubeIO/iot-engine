package com.nubeiot.core.rpc;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import com.nubeiot.iotdata.IoTEntity;

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
