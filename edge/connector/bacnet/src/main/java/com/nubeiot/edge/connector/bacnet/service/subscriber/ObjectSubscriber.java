package com.nubeiot.edge.connector.bacnet.service.subscriber;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointThingMetadata;
import com.nubeiot.edge.module.datapoint.rpc.subscriber.AbstractProtocolSubscriber;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

import lombok.NonNull;

//TODO implement it
public final class ObjectSubscriber extends AbstractProtocolSubscriber<Point> implements BACnetSubscriber<Point> {

    ObjectSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull EntityMetadata context() {
        return PointThingMetadata.INSTANCE;
    }

    @Override
    protected Single<Point> doCreate(@NonNull Point pojo) {
        throw new UnsupportedOperationException("Not yet supported CREATE BACnet object");
    }

    @Override
    protected Single<Point> doUpdate(@NonNull Point pojo) {
        throw new UnsupportedOperationException("Not yet supported UPDATE BACnet object");
    }

    @Override
    protected Single<Point> doPatch(@NonNull Point pojo) {
        throw new UnsupportedOperationException("Not yet supported PATCH BACnet object");
    }

    @Override
    protected Single<Point> doDelete(@NonNull Point pojo) {
        throw new UnsupportedOperationException("Not yet supported DELETE BACnet object");
    }

}
