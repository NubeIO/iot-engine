package com.nubeiot.core.rpc;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import com.nubeiot.iotdata.IoTEntity;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public abstract class BaseRpcProtocol<P extends IoTEntity> implements RpcProtocol<P> {

    @NonNull
    private final SharedDataLocalProxy sharedData;

}
