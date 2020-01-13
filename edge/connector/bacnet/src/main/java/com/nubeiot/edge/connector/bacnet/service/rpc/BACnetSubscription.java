package com.nubeiot.edge.connector.bacnet.service.rpc;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.service.BACnetRpcClient;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.nubeiot.edge.module.datapoint.rpc.AbstractProtocolSubscription;

import lombok.NonNull;

public final class BACnetSubscription
    extends AbstractProtocolSubscription<BACnetSubscription, BACnetSubscriber<VertxPojo>>
    implements BACnetRpcClient<BACnetSubscription> {

    private final boolean isMaster;

    public BACnetSubscription(@NonNull Vertx vertx, @NonNull String sharedKey, boolean isMaster) {
        super(vertx, sharedKey);
        this.isMaster = isMaster;
    }

    public Single<JsonObject> register(@NonNull BACnetSubscriber<VertxPojo> subscriber) {
        return super.register(subscriber).onErrorReturn(throwable -> {
            BACnetRpcClient.sneakyThrowable(logger, throwable, isMaster);
            return new JsonObject();
        });
    }

    @Override
    public Single<JsonObject> unregister(@NonNull BACnetSubscriber<VertxPojo> subscriber) {
        return super.unregister(subscriber).onErrorReturn(throwable -> {
            BACnetRpcClient.sneakyThrowable(logger, throwable, isMaster);
            return new JsonObject();
        }).doOnSuccess(r -> subscribers().remove(subscriber));
    }

}
