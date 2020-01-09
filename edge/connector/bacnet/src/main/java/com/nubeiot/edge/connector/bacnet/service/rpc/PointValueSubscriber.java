package com.nubeiot.edge.connector.bacnet.service.rpc;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.rpc.AbstractProtocolSubscriber;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

import lombok.NonNull;

//TODO implement it
public final class PointValueSubscriber extends AbstractProtocolSubscriber<PointValueData>
    implements BACnetSubscriber<PointValueData> {

    PointValueSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull EntityMetadata metadata() {
        return PointValueMetadata.INSTANCE;
    }

    @Override
    public Single<PointValueData> create(@NonNull RequestData requestData) {
        return null;
    }

    @Override
    public Single<PointValueData> update(@NonNull RequestData requestData) {
        return null;
    }

    @Override
    public Single<PointValueData> patch(@NonNull RequestData requestData) {
        return null;
    }

    @Override
    public Single<PointValueData> delete(@NonNull RequestData requestData) {
        return null;
    }

}
