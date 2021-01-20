package com.nubeiot.core.rpc;

import io.github.zero88.qwe.component.SharedDataDelegate.AbstractSharedDataDelegate;
import io.vertx.core.Vertx;

import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

public abstract class BaseRpcProtocol<P extends IoTEntity, T extends BaseRpcProtocol>
    extends AbstractSharedDataDelegate<T> implements RpcProtocol<P> {

    protected BaseRpcProtocol(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx);
        this.registerSharedKey(sharedKey);
    }

}
