package com.nubeiot.edge.module.datapoint.rpc;

import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;

import lombok.NonNull;

public abstract class BaseRpcProtocol<T extends BaseRpcProtocol> extends AbstractSharedDataDelegate<T>
    implements RpcProtocol {

    protected BaseRpcProtocol(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx);
        this.registerSharedKey(sharedKey);
    }

}
