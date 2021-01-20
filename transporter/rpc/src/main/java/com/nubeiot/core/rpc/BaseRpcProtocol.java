package com.nubeiot.core.rpc;

import io.github.zero88.msa.bp.component.SharedDataDelegate.AbstractSharedDataDelegate;
import io.github.zero88.msa.bp.dto.JsonData;
import io.vertx.core.Vertx;

import lombok.NonNull;

public abstract class BaseRpcProtocol<P extends JsonData, T extends BaseRpcProtocol>
    extends AbstractSharedDataDelegate<T> implements RpcProtocol<P> {

    protected BaseRpcProtocol(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx);
        this.registerSharedKey(sharedKey);
    }

}
