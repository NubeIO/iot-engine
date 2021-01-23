package com.nubeiot.edge.connector.bacnet.service.subscriber;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.entity.BACnetEntity;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;

import lombok.NonNull;

public final class BACnetRpcSubscription
    /*extends AbstractProtocolRpcSubscription<BACnetRpcSubscription, BACnetSubscriber<BACnetEntity>>
    implements BACnetRpcProtocol<BACnetEntity>*/ {

    private final boolean isMaster;

    public BACnetRpcSubscription(@NonNull Vertx vertx, @NonNull String sharedKey, boolean isMaster) {
        //        super(vertx, sharedKey);
        this.isMaster = isMaster;
    }

    public Single<JsonObject> register(@NonNull BACnetSubscriber<BACnetEntity> subscriber) {
        //        return super.register(subscriber).onErrorReturn(throwable -> {
        //            BACnetRpcProtocol.sneakyThrowable(logger, throwable, isMaster);
        //            return new JsonObject();
        //        });
        return Single.just(new JsonObject());
    }

    public Single<JsonObject> unregister(@NonNull BACnetSubscriber<BACnetEntity> subscriber) {
        //        return super.unregister(subscriber).onErrorReturn(throwable -> {
        //            BACnetRpcProtocol.sneakyThrowable(logger, throwable, isMaster);
        //            return new JsonObject();
        //        }).doOnSuccess(r -> subscribers().remove(subscriber));
        return Single.just(new JsonObject());
    }

}
