package com.nubeiot.core.rpc.subscriber;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.Vertx;

import com.nubeiot.core.rpc.BaseRpcProtocol;
import com.nubeiot.iotdata.IoTEntity;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public abstract class AbstractProtocolRpcSubscription<P extends IoTEntity, T extends AbstractProtocolRpcSubscription,
                                                         S extends RpcSubscriber<P>>
    extends BaseRpcProtocol<P, T> implements RpcSubscription/*<P, T, S>*/ {

    @Getter
    private final Set<S> subscribers = new HashSet<>();

    public AbstractProtocolRpcSubscription(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

}
