package com.nubeiot.edge.connector.bacnet.service.subscriber;

import io.vertx.core.Vertx;

import lombok.NonNull;

//TODO implement it
public final class PointValueSubscriber /*extends AbstractProtocolSubscriber<PointValueData>
    implements BACnetSubscriber<PointValueData>*/ {

    PointValueSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        /*super(vertx, sharedKey);*/
    }

    //    @Override
    //    public @NonNull Class<PointValueData> context() {
    //        return PointValueMetadata.INSTANCE;
    //    }
    //
    //    @Override
    //    protected Single<PointValueData> doCreate(@NonNull PointValueData pojo) {
    //        throw new UnsupportedOperationException("Not yet supported CREATE BACnet Point Value");
    //    }
    //
    //    @Override
    //    protected Single<PointValueData> doUpdate(@NonNull PointValueData pojo) {
    //        throw new UnsupportedOperationException("Not yet supported UPDATE BACnet Point Value");
    //    }
    //
    //    @Override
    //    protected Single<PointValueData> doPatch(@NonNull PointValueData pojo) {
    //        throw new UnsupportedOperationException("Not yet supported PATCH BACnet Point Value");
    //    }
    //
    //    @Override
    //    protected Single<PointValueData> doDelete(@NonNull PointValueData pojo) {
    //        throw new UnsupportedOperationException("Not yet supported DELETE BACnet Point Value");
    //    }
}
