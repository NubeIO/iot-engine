package com.nubeiot.edge.connector.bacnet.service.discovery;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventAction;
import io.github.zero88.qwe.event.EventContractor;
import io.github.zero88.qwe.exceptions.AlreadyExistException;
import io.github.zero88.qwe.exceptions.CarlException;
import io.github.zero88.qwe.exceptions.EngineException;
import io.github.zero88.qwe.exceptions.NotFoundException;
import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.github.zero88.utils.Functions;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryParams;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryResponse;
import com.nubeiot.edge.connector.bacnet.entity.BACnetPointEntity;
import com.nubeiot.edge.connector.bacnet.internal.request.WritePointValueRequestFactory;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectPropertyValues;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class BACnetObjectExplorer extends AbstractBACnetExplorer<BACnetPointEntity>
    implements BACnetExplorer<BACnetPointEntity> {

    BACnetObjectExplorer(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    public @NonNull Class<BACnetPointEntity> context() {
        return BACnetPointEntity.class;
    }

    @Override
    public @NonNull String servicePath() {
        return "/network/:" + DiscoveryParams.Fields.networkCode + "/device/:" + DiscoveryParams.Fields.deviceInstance +
               "/object";
    }

    @Override
    public String paramPath() {
        return DiscoveryParams.Fields.objectCode;
    }

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        //TODO PATCH is temporary
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
    public Single<JsonObject> discover(RequestData requestData) {
        return doGet(createDiscoveryArgs(requestData, DiscoveryLevel.OBJECT)).map(PropertyValuesMixin::toJson);
    }

    @Override
    public Single<JsonObject> discoverMany(RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, DiscoveryLevel.DEVICE);
        final BACnetDevice device = getLocalDeviceFromCache(args);
        log.info("Discovering objects in device '{}' in network {}...",
                 ObjectIdentifierMixin.serialize(args.remoteDeviceId()), device.protocol().identifier());
        return device.discoverRemoteDevice(args.remoteDeviceId(), args.options())
                     .flatMap(remote -> getRemoteObjects(device, remote, args.options().isDetail()))
                     .map(opv -> DiscoveryResponse.builder().objects(opv).build())
                     .map(DiscoveryResponse::toJson)
                     .doFinally(device::stop);
    }

    @EventContractor(action = "PATCH", returnType = Single.class)
    public Single<JsonObject> patchPointValue(RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, DiscoveryLevel.OBJECT);
        final BACnetDevice device = getLocalDeviceFromCache(args);
        return device.send(EventAction.PATCH, args, requestData, new WritePointValueRequestFactory())
                     .map(JsonData::toJson);
    }

    @Override
    protected String parseResourceId(@NonNull JsonObject resource) {
        return resource.getJsonObject("point", new JsonObject()).getString("id");
    }

    private Single<ObjectPropertyValues> getRemoteObjects(@NonNull BACnetDevice device, @NonNull RemoteDevice rd,
                                                          boolean detail) {
        return Observable.fromIterable(Functions.getOrThrow(t -> new CarlException(EngineException.CODE, t),
                                                            () -> RequestUtils.getObjectList(device.localDevice(), rd)))
                         .filter(objId -> objId.getObjectType() != ObjectType.device)
                         .flatMapSingle(objId -> this.parseRemoteObject(device, rd, objId, detail, false)
                                                     .map(props -> new SimpleEntry<>(objId, props)))
                         .collect(ObjectPropertyValues::new,
                                  (values, entry) -> values.add(entry.getKey(), entry.getValue()))
                         .doFinally(device::stop);
    }

    private Single<PropertyValuesMixin> doGet(@NonNull DiscoveryArguments args) {
        final BACnetDevice device = getLocalDeviceFromCache(args);
        log.info("Discovering object '{}' in device '{}' in network {}...",
                 ObjectIdentifierMixin.serialize(args.objectCode()),
                 ObjectIdentifierMixin.serialize(args.remoteDeviceId()), device.protocol().identifier());
        return device.discoverRemoteDevice(args.remoteDeviceId(), args.options())
                     .flatMap(rd -> parseRemoteObject(device, rd, args.objectCode(), true, args.options().isDetail()));
    }

    private DiscoveryArguments validateCache(@NonNull DiscoveryArguments args) {
        final BACnetDevice device = getLocalDeviceFromCache(args);
        networkCache().getDataKey(device.protocol().identifier())
                      .orElseThrow(() -> new NotFoundException(
                          "Not found a persistence network by network code " + device.protocol().identifier()));
        deviceCache().getDataKey(device.protocol(), args.remoteDeviceId())
                     .orElseThrow(() -> new NotFoundException(
                         "Not found a persistence device by remote device " + args.remoteDeviceId()));
        final Optional<UUID> objectId = objectCache().getDataKey(device.protocol(), args.remoteDeviceId(),
                                                                 args.objectCode());
        if (objectId.isPresent()) {
            throw new AlreadyExistException(
                "Already existed object " + ObjectIdentifierMixin.serialize(args.objectCode()) + " in device " +
                args.remoteDeviceId() + " in network " + device.protocol().identifier());
        }
        return args;
    }

}
