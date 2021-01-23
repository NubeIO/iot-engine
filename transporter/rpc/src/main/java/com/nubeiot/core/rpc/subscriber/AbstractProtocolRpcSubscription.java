package com.nubeiot.core.rpc.subscriber;

import java.util.HashSet;
import java.util.Set;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import com.nubeiot.core.rpc.BaseRpcProtocol;
import com.nubeiot.iotdata.IoTEntity;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public abstract class AbstractProtocolRpcSubscription<P extends IoTEntity, S extends RpcSubscriber<P>>
    extends BaseRpcProtocol<P> implements RpcSubscription<P, S> {

    @Getter
    private final Set<S> subscribers = new HashSet<>();

    public AbstractProtocolRpcSubscription(@NonNull SharedDataLocalProxy proxy) {
        super(proxy);
    }

}
