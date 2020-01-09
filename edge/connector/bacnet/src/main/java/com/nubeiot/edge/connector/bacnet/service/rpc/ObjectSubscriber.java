package com.nubeiot.edge.connector.bacnet.service.rpc;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.nubeiot.edge.module.datapoint.rpc.AbstractProtocolSubscriber;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

import lombok.NonNull;

//TODO implement it
public final class ObjectSubscriber extends AbstractProtocolSubscriber<Point> implements BACnetSubscriber<Point> {

    ObjectSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull EntityMetadata metadata() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public Single<Point> create(RequestData requestData) {
        return null;
    }

    @Override
    public Single<Point> update(RequestData requestData) {
        return null;
    }

    @Override
    public Single<Point> patch(RequestData requestData) {
        return null;
    }

    @Override
    public Single<Point> delete(RequestData requestData) {
        return null;
    }

}
