package com.nubeiot.edge.connector.bacnet.service.subscriber;

import io.vertx.core.Vertx;

import lombok.NonNull;

//TODO implement it
public final class NetworkSubscriber /*extends AbstractProtocolSubscriber<Network> implements
BACnetSubscriber<Network>*/ {

    NetworkSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        /*super(vertx, sharedKey);*/
    }
    //
    //    @Override
    //    public @NonNull Class<Network> context() {
    //        return NetworkMetadata.INSTANCE;
    //    }
    //
    //    @Override
    //    protected Single<Network> doCreate(@NonNull Network pojo) {
    //        throw new UnsupportedOperationException("Not yet supported CREATE BACnet network");
    //    }
    //
    //    @Override
    //    protected Single<Network> doUpdate(@NonNull Network pojo) {
    //        throw new UnsupportedOperationException("Not yet supported UPDATE BACnet network");
    //    }
    //
    //    @Override
    //    protected Single<Network> doPatch(@NonNull Network pojo) {
    //        throw new UnsupportedOperationException("Not yet supported UPDATE BACnet network");
    //    }
    //
    //    @Override
    //    protected Single<Network> doDelete(@NonNull Network pojo) {
    //        throw new UnsupportedOperationException("Not yet supported DELETE BACnet network");
    //    }
}
