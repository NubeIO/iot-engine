package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.List;
import java.util.function.Function;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.converter.ObjectIdentifierConverter;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.DiscoverLevel;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.Fields;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.ObjectPropertyTypeDefinition;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.ObjectPropertyReference;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyReferences;
import com.serotonin.bacnet4j.util.PropertyValues;
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
                     .doFinally(device::stop);
    }

    @Override
    public Single<JsonObject> get(RequestData reqData) {
        final DiscoverRequest request = DiscoverRequest.from(reqData, DiscoverLevel.OBJECT);
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        final CommunicationProtocol protocol = parseNetworkProtocol(request);
        logger.info("Discover object '{}' in device '{}' by network {}", request.getObjectCode(),
                    request.getDeviceCode(), protocol.toJson());
        final BACnetDeviceCache cache = getSharedDataValue(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
        final BACnetDevice device = cache.get(protocol);
        final ObjectIdentifier objId = ObjectIdentifierConverter.toBACnetId(request.getObjectCode());
        return device.discoverRemoteDevice(request.getDeviceCode(), options)
                     .flatMap(
                         remote -> getRemoteObject(device.getLocalDevice(), remote, objId, true, options.isDetail()))
                     .doFinally(device::stop);
    }

    @Override
    public Single<JsonObject> batchPersist(RequestData reqData) {
        return Single.just(new JsonObject());
    }

    @Override
    public Single<JsonObject> persist(RequestData reqData) {
        return Single.just(new JsonObject());
    }

    private Single<JsonObject> getRemoteObjects(@NonNull LocalDevice local, @NonNull RemoteDevice remoteDevice,
                                                boolean detail) {
        return Observable.fromIterable(Functions.getOrThrow(t -> new NubeException(ErrorCode.ENGINE_ERROR, t),
                                                            () -> RequestUtils.getObjectList(local, remoteDevice)))
                         .filter(objId -> objId.getObjectType() != ObjectType.device)
                         .flatMap(objId -> getRemoteObject(local, remoteDevice, objId, detail, false).map(
                             r -> new JsonObject().put(ObjectIdentifierConverter.toRequestId(objId), r)).toObservable())
                         .collectInto(new JsonObject(), JsonObject::mergeIn);
    }

    private Single<JsonObject> getRemoteObject(@NonNull LocalDevice localDevice, @NonNull RemoteDevice remoteDevice,
                                               @NonNull ObjectIdentifier objId, boolean detail, boolean includeError) {
        Function<ObjectType, List<ObjectPropertyTypeDefinition>> f = detail
                                                                     ?
                                                                     ObjectProperties::getObjectPropertyTypeDefinitions
                                                                     :
                                                                     ObjectProperties::getRequiredObjectPropertyTypeDefinitions;
        return Observable.fromIterable(f.apply(objId.getObjectType()))
                         .map(definition -> new ObjectPropertyReference(objId, definition.getPropertyTypeDefinition()
                                                                                         .getPropertyIdentifier()))
                         .collect(PropertyReferences::new,
                                  (refs, opr) -> refs.addIndex(objId, opr.getPropertyIdentifier(),
                                                               opr.getPropertyArrayIndex()))
                         .map(propRefs -> RequestUtils.readProperties(localDevice, remoteDevice, propRefs, true, null))
                         .map(pvs -> convertPropertyValues(pvs, includeError));
    }

    private JsonObject convertPropertyValues(PropertyValues pvs, boolean includeError) {
        JsonObject json = new JsonObject();
        pvs.iterator().forEachRemaining(opr -> {
            final Encodable value = pvs.getNoErrorCheck(opr);
            if (value instanceof ErrorClassAndCode && !includeError) {
                return;
            }
            json.put(opr.getPropertyIdentifier().toString(), value.toString());
        });
        return json;
    }

}
