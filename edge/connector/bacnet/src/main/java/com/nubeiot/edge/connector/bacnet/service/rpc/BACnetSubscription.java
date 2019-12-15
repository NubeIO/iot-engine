package com.nubeiot.edge.connector.bacnet.service.rpc;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcClient;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.nubeiot.edge.module.datapoint.rpc.DataProtocolSubscription;

import lombok.NonNull;

public final class BACnetSubscription extends AbstractSharedDataDelegate<BACnetSubscription>
    implements DataProtocolSubscription<BACnetSubscription>, BACnetRpcClient<BACnetSubscription> {

    private final boolean isMaster;

    public BACnetSubscription(@NonNull Vertx vertx, @NonNull String sharedKey, boolean isMaster) {
        super(vertx);
        this.registerSharedKey(sharedKey);
        this.isMaster = isMaster;
    }

    @SuppressWarnings("unchecked")
    public Single<BACnetSubscriber> doRegister(@NonNull BACnetSubscriber subscriber) {
        return register(subscriber).doOnSuccess(logger::info).map(ignore -> subscriber).onErrorReturn(t -> {
            BACnetRpcClient.sneakyThrowable(logger, (Throwable) t, isMaster);
            return subscriber;
        });
    }

}
