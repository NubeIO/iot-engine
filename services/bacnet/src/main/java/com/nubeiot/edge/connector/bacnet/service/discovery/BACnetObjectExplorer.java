package com.nubeiot.edge.connector.bacnet.service.discovery;

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
import io.github.zero88.qwe.exceptions.ErrorCode;
import io.github.zero88.qwe.exceptions.NotFoundException;
import io.github.zero88.qwe.iot.data.entity.AbstractEntities;
import io.github.zero88.qwe.micro.http.ActionMethodMapping;
import io.github.zero88.utils.Functions;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.entity.BACnetEntities.BACnetPoints;
import com.nubeiot.edge.connector.bacnet.entity.BACnetPointEntity;
import com.nubeiot.edge.connector.bacnet.internal.request.WritePointValueRequestFactory;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.RequestUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class BACnetObjectExplorer
    extends AbstractBACnetExplorer<ObjectIdentifier, BACnetPointEntity, BACnetPoints> {

    BACnetObjectExplorer(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
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
    public Single<BACnetPointEntity> discover(RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, level());
        final BACnetDevice device = getLocalDeviceFromCache(args);
        log.info("Discovering object '{}' in device '{}' in network {}...",
                 ObjectIdentifierMixin.serialize(args.params().objectCode()),
                 ObjectIdentifierMixin.serialize(args.params().remoteDeviceId()), device.protocol().identifier());
        return device.discoverRemoteDevice(args.params().remoteDeviceId(), args.options())
                     .flatMap(rd -> parseRemoteObject(device, rd, args.params().objectCode(), true,
                                                      args.options().isDetail()))
                     .map(pvm -> BACnetPointEntity.from(args.params().getNetworkId(), args.params().remoteDeviceId(),
                                                        pvm));
    }

    @Override
    public Single<BACnetPoints> discoverMany(RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, DiscoveryLevel.DEVICE);
        final BACnetDevice device = getLocalDeviceFromCache(args);
        log.info("Discovering objects in device '{}' in network {}...",
                 ObjectIdentifierMixin.serialize(args.params().remoteDeviceId()), device.protocol().identifier());
        return device.discoverRemoteDevice(args.params().remoteDeviceId(), args.options())
                     .flatMap(remote -> getRemoteObjects(device, remote, args.options().isDetail()))
                     .doFinally(device::stop);
    }

    @EventContractor(action = "PATCH", returnType = Single.class)
    public Single<JsonObject> patchPointValue(RequestData requestData) {
        final DiscoveryArguments args = createDiscoveryArgs(requestData, level());
        final BACnetDevice device = getLocalDeviceFromCache(args);
        return device.send(EventAction.PATCH, args, requestData, new WritePointValueRequestFactory())
                     .map(JsonData::toJson);
    }

    @Override
    public DiscoveryLevel level() {
        return DiscoveryLevel.OBJECT;
    }

    private Single<BACnetPoints> getRemoteObjects(@NonNull BACnetDevice device, @NonNull RemoteDevice rd,
                                                  boolean detail) {
        return Observable.fromIterable(Functions.getOrThrow(t -> new CarlException(ErrorCode.SERVICE_ERROR, t),
                                                            () -> RequestUtils.getObjectList(device.localDevice(), rd)))
                         .filter(oid -> oid.getObjectType() != ObjectType.device)
                         .flatMapSingle(oid -> this.parseRemoteObject(device, rd, oid, detail, false)
                                                   .map(pvm -> BACnetPointEntity.from(device.protocol().identifier(),
                                                                                      rd.getObjectIdentifier(), pvm)))
                         .collect(BACnetPoints::new, AbstractEntities::add)
                         .doFinally(device::stop);
    }

    private DiscoveryArguments validateCache(@NonNull DiscoveryArguments args) {
        final BACnetDevice device = getLocalDeviceFromCache(args);
        networkCache().getDataKey(device.protocol().identifier())
                      .orElseThrow(() -> new NotFoundException(
                          "Not found a persistence network by network code " + device.protocol().identifier()));
        deviceCache().getDataKey(device.protocol(), args.params().remoteDeviceId())
                     .orElseThrow(() -> new NotFoundException(
                         "Not found a persistence device by remote device " + args.params().remoteDeviceId()));
        final Optional<UUID> objectId = objectCache().getDataKey(device.protocol(), args.params().remoteDeviceId(),
                                                                 args.params().objectCode());
        if (objectId.isPresent()) {
            throw new AlreadyExistException(
                "Already existed object " + ObjectIdentifierMixin.serialize(args.params().objectCode()) +
                " in device " + args.params().remoteDeviceId() + " in network " + device.protocol().identifier());
        }
        return args;
    }

}
