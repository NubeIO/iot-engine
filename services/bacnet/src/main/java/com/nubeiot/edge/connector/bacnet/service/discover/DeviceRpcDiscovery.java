package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Optional;
import java.util.UUID;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.exceptions.AlreadyExistException;
import io.github.zero88.qwe.exceptions.NotFoundException;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverLevel;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.Fields;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.discover.RemoteDeviceScanner;
import com.nubeiot.edge.connector.bacnet.entity.BACnetDeviceEntity;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DeviceRpcDiscovery extends AbstractBACnetRpcDiscoveryService<BACnetDeviceEntity>
    implements BACnetRpcDiscoveryService<BACnetDeviceEntity> {

    DeviceRpcDiscovery(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy);
    }

    @Override
    public @NonNull Class<BACnetDeviceEntity> context() {
        return BACnetDeviceEntity.class;
    }

    @Override
    public @NonNull String servicePath() {
        return "/network/:" + Fields.networkCode + "/device";
    }

    @Override
    public String paramPath() {
        return Fields.deviceInstance;
    }

    @Override
    public Single<JsonObject> list(RequestData reqData) {
        final DiscoveryRequestWrapper request = createDiscoveryRequest(reqData, DiscoverLevel.NETWORK);
        log.info("Discovering devices in network {}...", request.device().protocol().identifier());
        return request.device()
                      .scanRemoteDevices(request.options())
                      .map(RemoteDeviceScanner::getRemoteDevices)
                      .flattenAsObservable(r -> r)
                      .flatMapSingle(rd -> parseRemoteDevice(request.device(), rd, request.options().isDetail(), false))
                      .toList()
                      .map(results -> DiscoverResponse.builder().remoteDevices(results).build().toJson())
                      .doFinally(request.device()::stop);
    }

    @Override
    public Single<JsonObject> get(RequestData reqData) {
        final DiscoveryRequestWrapper request = createDiscoveryRequest(reqData, DiscoverLevel.DEVICE);
        return doGet(request).map(RemoteDeviceMixin::toJson);
    }

    @Override
    public Single<JsonObject> discoverThenRegisterMany(RequestData reqData) {
        return watchMany(reqData.body());
    }

    @Override
    public Single<JsonObject> discoverThenRegisterOne(RequestData requestData) {
        return Single.just(new JsonObject());
        //        final DiscoveryRequestWrapper req = validateCache(toRequest(requestData, DiscoverLevel.DEVICE));
        //        return doGet(req).map(rd -> new BACnetDeviceConverter().serialize(rd))
        //                         .map(pojo -> JsonPojo.from(pojo).toJson())
        //                         .flatMap(this::doPersist)
        //                         .doOnSuccess(r -> deviceCache().addDataKey(req.device().protocol(), req
        //                         .remoteDeviceId(),
        //                                                                    parsePersistResponse(r)));
    }

    @Override
    protected String parseResourceId(JsonObject resource) {
        return resource.getJsonObject("device", new JsonObject()).getString("id");
    }

    private DiscoveryRequestWrapper validateCache(@NonNull DiscoveryRequestWrapper request) {
        networkCache().getDataKey(request.device().protocol().identifier())
                      .orElseThrow(() -> new NotFoundException("Not found a persistence network by network code " +
                                                               request.device().protocol().identifier()));
        final Optional<UUID> deviceId = deviceCache().getDataKey(request.device().protocol(), request.remoteDeviceId());
        if (deviceId.isPresent()) {
            throw new AlreadyExistException(
                "Already existed device " + ObjectIdentifierMixin.serialize(request.remoteDeviceId()) + " in network " +
                request.device().protocol().identifier());
        }
        return request;
    }

    private Single<RemoteDeviceMixin> doGet(@NonNull DiscoveryRequestWrapper request) {
        log.info("Discovering remote device {} in network {}...",
                 ObjectIdentifierMixin.serialize(request.remoteDeviceId()), request.device().protocol().identifier());
        return request.device()
                      .discoverRemoteDevice(request.remoteDeviceId(), request.options())
                      .flatMap(rd -> parseRemoteDevice(request.device(), rd, true, request.options().isDetail()))
                      .doFinally(request.device()::stop);
    }

    private Single<RemoteDeviceMixin> parseRemoteDevice(@NonNull BACnetDevice device, @NonNull RemoteDevice rd, boolean detail, boolean includeError) {
        final ObjectIdentifier objId = rd.getObjectIdentifier();
        final String networkCode = device.protocol().identifier();
        final UUID networkId = networkCache().getDataKey(networkCode).orElse(null);
        return parseRemoteObject(device, rd, objId, detail, includeError).map(pvm -> RemoteDeviceMixin.create(rd, pvm))
                                                                         .map(rdm -> rdm.setNetworkCode(networkCode)
                                                                                        .setNetworkId(networkId));
    }

}
