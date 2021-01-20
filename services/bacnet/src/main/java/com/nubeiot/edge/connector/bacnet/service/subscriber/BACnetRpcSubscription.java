package com.nubeiot.edge.connector.bacnet.service.subscriber;

import io.github.zero88.msa.bp.dto.JsonData;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.rpc.subscriber.AbstractProtocolRpcSubscription;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;

import lombok.NonNull;

public final class BACnetRpcSubscription
    extends AbstractProtocolRpcSubscription<BACnetRpcSubscription, BACnetSubscriber<JsonData>>
    implements BACnetRpcProtocol {

    private final boolean isMaster;

    public BACnetRpcSubscription(@NonNull Vertx vertx, @NonNull String sharedKey, boolean isMaster) {
        super(vertx, sharedKey);
        this.isMaster = isMaster;
    }

    public Single<JsonObject> register(@NonNull BACnetSubscriber<JsonData> subscriber) {
        return super.register(subscriber).onErrorReturn(throwable -> {
            BACnetRpcProtocol.sneakyThrowable(logger, throwable, isMaster);
            return new JsonObject();
        });
    }

    @Override
    public Single<JsonObject> unregister(@NonNull BACnetSubscriber<JsonData> subscriber) {
        return super.unregister(subscriber).onErrorReturn(throwable -> {
            BACnetRpcProtocol.sneakyThrowable(logger, throwable, isMaster);
            return new JsonObject();
        }).doOnSuccess(r -> subscribers().remove(subscriber));
    }

}
