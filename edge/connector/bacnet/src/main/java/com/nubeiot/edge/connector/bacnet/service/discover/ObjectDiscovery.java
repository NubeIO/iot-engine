package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.DiscoverLevel;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.Fields;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectPropertyValues;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.translator.BACnetPointTranslator;
import com.nubeiot.edge.module.datapoint.DataPointIndex.PointCompositeMetadata;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.NonNull;

public final class ObjectDiscovery extends AbstractDiscoveryService implements BACnetDiscoveryService {

    ObjectDiscovery(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull EntityMetadata context() {
        return PointCompositeMetadata.INSTANCE;
    }

    @Override
    public @NonNull String servicePath() {
        return "/network/:" + Fields.networkCode + "/device/:" + Fields.deviceInstance + "/object";
    }

    @Override
    public String paramPath() {
        return Fields.objectCode;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        //TODO temporary
        return Stream.concat(super.getAvailableEvents().stream(), Stream.of(EventAction.PATCH))
                     .collect(Collectors.toSet());
    }

    @Override
    public @NonNull ActionMethodMapping eventMethodMap() {
        final Map<EventAction, HttpMethod> map = new HashMap<>(super.eventMethodMap().get());
        map.put(EventAction.PATCH, HttpMethod.PATCH);
        return ActionMethodMapping.create(map);
    }

    @Override
    public Single<JsonObject> list(RequestData requestData) {
        final DiscoveryRequestWrapper request = toRequest(requestData, DiscoverLevel.DEVICE);
        logger.info("Discovering objects in device '{}' in network {}...", request.remoteDeviceId(),
                    request.device().protocol().identifier());
        return request.device()
                      .discoverRemoteDevice(request.remoteDeviceId(), request.options())
                      .flatMap(remote -> getRemoteObjects(request.device(), remote, request.options().isDetail()))
                      .map(opv -> DiscoverResponse.builder().objects(opv).build())
                      .map(DiscoverResponse::toJson)
                      .doFinally(request.device()::stop);
    }

    @Override
    public Single<JsonObject> get(RequestData requestData) {
        return doGet(toRequest(requestData, DiscoverLevel.OBJECT)).map(PropertyValuesMixin::toJson);
    }

    @Override
    public Single<JsonObject> discoverThenDoBatch(RequestData requestData) {
        return doBatch(requestData.body());
    }

    @Override
    public Single<JsonObject> discoverThenDoPersist(RequestData requestData) {
        final DiscoveryRequestWrapper request = validateCache(toRequest(requestData, DiscoverLevel.OBJECT));
        return doGet(request).map(properties -> new BACnetPointTranslator().serialize(properties))
                             .map(pojo -> JsonPojo.from(pojo).toJson())
                             .flatMap(this::doPersist)
                             .doOnSuccess(response -> objectCache().addDataKey(request.device().protocol(),
                                                                               request.remoteDeviceId(),
                                                                               request.objectCode(),
                                                                               parsePersistResponse(response)));
    }

    @Override
    protected String parseResourceId(@NonNull JsonObject resource) {
        return resource.getJsonObject("point", new JsonObject()).getString("id");
    }

    @EventContractor(action = EventAction.PATCH, returnType = Single.class)
    public Single<JsonObject> patchValue(RequestData requestData) {
        final DiscoverRequest request = DiscoverRequest.from(requestData, DiscoverLevel.OBJECT);
        return Single.just(new JsonObject());
    }

    private Single<ObjectPropertyValues> getRemoteObjects(@NonNull BACnetDevice device, @NonNull RemoteDevice rd,
                                                          boolean detail) {
        return Observable.fromIterable(Functions.getOrThrow(t -> new NubeException(ErrorCode.ENGINE_ERROR, t),
                                                            () -> RequestUtils.getObjectList(device.localDevice(), rd)))
                         .filter(objId -> objId.getObjectType() != ObjectType.device)
                         .flatMapSingle(objId -> parseRemoteObject(device, rd, objId, detail, false).map(
                             props -> new SimpleEntry<>(objId, props)))
                         .collect(ObjectPropertyValues::new,
                                  (values, entry) -> values.add(entry.getKey(), entry.getValue()));
    }

    private Single<PropertyValuesMixin> doGet(@NonNull DiscoveryRequestWrapper request) {
        logger.info("Discovering object '{}' in device '{}' in network {}...", request.objectCode(),
                    request.remoteDeviceId(), request.device().protocol().identifier());
        return request.device()
                      .discoverRemoteDevice(request.remoteDeviceId(), request.options())
                      .flatMap(rd -> parseRemoteObject(request.device(), rd, request.objectCode(), true,
                                                       request.options().isDetail()))
                      .doFinally(request.device()::stop);
    }

    private DiscoveryRequestWrapper validateCache(@NonNull DiscoveryRequestWrapper request) {
        networkCache().getDataKey(request.device().protocol().identifier())
                      .orElseThrow(
                          () -> new NotFoundException("Not found network information. Need to persist network"));
        deviceCache().getDataKey(request.device().protocol(), request.remoteDeviceId())
                     .orElseThrow(() -> new NotFoundException("Not found device information. Need to persist device"));
        final Optional<UUID> objectId = objectCache().getDataKey(request.device().protocol(), request.remoteDeviceId(),
                                                                 request.objectCode());
        if (objectId.isPresent()) {
            throw new AlreadyExistException(
                "Already existed object " + ObjectIdentifierMixin.serialize(request.objectCode()) + " in device " +
                request.remoteDeviceId() + " in network " + request.device().protocol().identifier());
        }
        return request;
    }

}
