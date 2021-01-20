package com.nubeiot.edge.connector.bacnet.service.subscriber;

import java.util.Objects;

import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventMessage;
import io.github.zero88.qwe.exceptions.converter.CarlExceptionConverter;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.mixin.BACnetExceptionConverter;
import com.nubeiot.edge.connector.bacnet.mixin.deserializer.EncodableDeserializer;
import com.nubeiot.edge.connector.bacnet.service.discover.BACnetRpcDiscoveryService;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.NonNull;

//TODO implement it
public final class PointValueSubscriber /*extends AbstractProtocolSubscriber<PointValueData>
    implements BACnetSubscriber<PointValueData>*/ {

    PointValueSubscriber(@NonNull Vertx vertx, @NonNull String sharedKey) {
        /*super(vertx, sharedKey);*/
    }

    //TODO refactor it
    public static Single<JsonObject> write(@NonNull BACnetRpcDiscoveryService.DiscoveryRequestWrapper request,
                                           JsonObject pointValueData) {
        final Encodable encodable = EncodableDeserializer.parse(request.objectCode(), PropertyIdentifier.presentValue,
                                                                pointValueData.remove("value"));
        if (Objects.isNull(encodable)) {
            return Single.error(new IllegalArgumentException("Unrecognized value"));
        }
        //        final int priority = PointValueMetadata.INSTANCE.parseFromRequest(pointValueData).getPriority();
        final int priority = pointValueData.getInteger("priority");
        return request.device().discoverRemoteDevice(request.remoteDeviceId(), request.options()).map(rd -> {
            RequestUtils.writeProperty(request.device().localDevice(), rd, request.objectCode(),
                                       PropertyIdentifier.presentValue, encodable, priority);
            return EventMessage.success(EventAction.PATCH).toJson();
        }).onErrorReturn(throwable -> {
            if (throwable instanceof BACnetException) {
                throw BACnetExceptionConverter.convert((BACnetException) throwable);
            }
            throw CarlExceptionConverter.friendly(throwable);
        });
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
