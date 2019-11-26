package com.nubeiot.edge.connector.bacnet.service.discover;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.DiscoverLevel;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.Fields;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.dto.ObjectPropertyValues;
import com.nubeiot.edge.connector.bacnet.dto.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierSerializer;
import com.nubeiot.edge.connector.bacnet.translator.BACnetPointTranslator;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.NonNull;

public final class ObjectDiscovery extends AbstractBACnetDiscoveryService implements BACnetDiscoveryService {

    ObjectDiscovery(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull String servicePath() {
        return "/discovery/bacnet/network/:" + Fields.networkCode + "/device/:" + Fields.deviceCode + "/object";
    }

    @Override
    public String paramPath() {
        return Fields.objectCode;
    }

    @Override
    public Single<JsonObject> list(RequestData reqData) {
        final DiscoverRequest request = DiscoverRequest.from(reqData, DiscoverLevel.DEVICE);
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        final CommunicationProtocol protocol = parseNetworkProtocol(request);
        logger.info("Discover objects in device '{}' by network {}", request.getDeviceCode(), protocol.toJson());
        final BACnetDeviceCache cache = getSharedDataValue(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
        final BACnetDevice device = cache.get(protocol);
        return device.discoverRemoteDevice(request.getDeviceCode(), options)
                     .flatMap(remote -> getRemoteObjects(device.getLocalDevice(), remote, options.isDetail()))
                     .map(opv -> DiscoverResponse.builder().objects(opv).build())
                     .map(DiscoverResponse::toJson)
                     .doFinally(device::stop);
    }

    @Override
    public Single<JsonObject> get(RequestData reqData) {
        return doGet(reqData).map(PropertyValuesMixin::toJson);
    }

    @Override
    public Single<JsonObject> batchPersist(RequestData reqData) {
        return Single.just(new JsonObject());
    }

    @Override
    public Single<JsonObject> persist(RequestData reqData) {
        return doGet(reqData).map(properties -> new BACnetPointTranslator().serialize(properties))
                             .flatMap(point -> doPersist(EventAction.CREATE, point.toJson()));
    }

    @Override
    public String destination() {
        return DataPointIndex.lookupApiName(PointCompositeMetadata.INSTANCE);
    }

    private Single<ObjectPropertyValues> getRemoteObjects(@NonNull LocalDevice local, @NonNull RemoteDevice rd,
                                                          boolean detail) {
        return Observable.fromIterable(Functions.getOrThrow(t -> new NubeException(ErrorCode.ENGINE_ERROR, t),
                                                            () -> RequestUtils.getObjectList(local, rd)))
                         .filter(objId -> objId.getObjectType() != ObjectType.device)
                         .flatMapSingle(objId -> parseRemoteObject(local, rd, objId, detail, false))
                         .collect(ObjectPropertyValues::new, ObjectPropertyValues::add);
    }

    private Single<PropertyValuesMixin> doGet(RequestData reqData) {
        final DiscoverRequest request = DiscoverRequest.from(reqData, DiscoverLevel.OBJECT);
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        final CommunicationProtocol protocol = parseNetworkProtocol(request);
        logger.info("Discover object '{}' in device '{}' by network {}", request.getObjectCode(),
                    request.getDeviceCode(), protocol.toJson());
        final BACnetDeviceCache cache = getSharedDataValue(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
        final BACnetDevice device = cache.get(protocol);
        final ObjectIdentifier objId = ObjectIdentifierSerializer.deserialize(request.getObjectCode());
        return device.discoverRemoteDevice(request.getDeviceCode(), options)
                     .flatMap(rd -> parseRemoteObject(device.getLocalDevice(), rd, objId, true, options.isDetail()))
                     .doFinally(device::stop);
    }

}
