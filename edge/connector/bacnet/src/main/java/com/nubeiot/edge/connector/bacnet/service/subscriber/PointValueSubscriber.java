package com.nubeiot.edge.connector.bacnet.service.subscriber;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.connector.bacnet.service.BACnetSubscriber;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointValueMetadata;
import com.nubeiot.edge.module.datapoint.rpc.subscriber.AbstractProtocolSubscriber;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

import lombok.NonNull;

//TODO implement it
public final class PointValueSubscriber extends AbstractProtocolSubscriber<PointValueData>
    implements BACnetSubscriber<PointValueData> {

    PointValueSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull EntityMetadata context() {
        return PointValueMetadata.INSTANCE;
    }

    @Override
    protected Single<PointValueData> doCreate(@NonNull PointValueData pojo) {
        throw new UnsupportedOperationException("Not yet supported CREATE BACnet Point Value");
    }

    @Override
    protected Single<PointValueData> doUpdate(@NonNull PointValueData pojo) {
        throw new UnsupportedOperationException("Not yet supported UPDATE BACnet Point Value");
    }

    @Override
    protected Single<PointValueData> doPatch(@NonNull PointValueData pojo) {
        throw new UnsupportedOperationException("Not yet supported PATCH BACnet Point Value");
    }

    @Override
    protected Single<PointValueData> doDelete(@NonNull PointValueData pojo) {
        throw new UnsupportedOperationException("Not yet supported DELETE BACnet Point Value");
    }

}
