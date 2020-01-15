package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Optional;
import java.util.UUID;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.DiscoverLevel;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.Fields;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.discover.RemoteDeviceScanner;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.nubeiot.edge.connector.bacnet.translator.BACnetDeviceTranslator;
import com.nubeiot.edge.module.datapoint.DataPointIndex.EdgeDeviceMetadata;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

public final class DeviceDiscovery extends AbstractDiscoveryService implements BACnetDiscoveryService {

    DeviceDiscovery(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull EntityMetadata context() {
        return EdgeDeviceMetadata.INSTANCE;
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
        final DiscoveryRequestWrapper request = toRequest(reqData, DiscoverLevel.NETWORK);
        logger.info("Discovering devices in network {}...", request.device().protocol().identifier());
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
        final DiscoveryRequestWrapper wrapper = toRequest(reqData, DiscoverLevel.DEVICE);
        return doGet(wrapper).map(RemoteDeviceMixin::toJson);
    }

    @Override
    public Single<JsonObject> discoverThenDoBatch(RequestData reqData) {
        return doBatch(reqData.body());
    }

    @Override
    public Single<JsonObject> discoverThenDoPersist(RequestData requestData) {
        final DiscoveryRequestWrapper req = validateCache(toRequest(requestData, DiscoverLevel.DEVICE));
        return doGet(req).map(rd -> new BACnetDeviceTranslator().serialize(rd))
                         .map(pojo -> JsonPojo.from(pojo).toJson())
                         .flatMap(this::doPersist)
                         .doOnSuccess(r -> deviceCache().addDataKey(req.device().protocol(), req.remoteDeviceId(),
                                                                    parsePersistResponse(r)));
    }

    @Override
    protected String parseResourceId(JsonObject resource) {
        return resource.getJsonObject("device", new JsonObject()).getString("id");
    }

    private DiscoveryRequestWrapper validateCache(@NonNull DiscoveryRequestWrapper request) {
        networkCache().getDataKey(request.device().protocol().identifier())
                      .orElseThrow(
                          () -> new NotFoundException("Not found network information. Need to persist network"));
        final Optional<UUID> deviceId = deviceCache().getDataKey(request.device().protocol(), request.remoteDeviceId());
        if (deviceId.isPresent()) {
            throw new AlreadyExistException(
                "Already existed device " + ObjectIdentifierMixin.serialize(request.remoteDeviceId()) + " in network " +
                request.device().protocol().identifier());
        }
        return request;
    }

    private Single<RemoteDeviceMixin> doGet(@NonNull DiscoveryRequestWrapper wrapper) {
        logger.info("Discovering remote device {} in network {}...", wrapper.remoteDeviceId(),
                    wrapper.device().protocol().identifier());
        return wrapper.device()
                      .discoverRemoteDevice(wrapper.remoteDeviceId(), wrapper.options())
                      .flatMap(rd -> parseRemoteDevice(wrapper.device(), rd, true, wrapper.options().isDetail()))
                      .doFinally(wrapper.device()::stop);
    }

    private Single<RemoteDeviceMixin> parseRemoteDevice(@NonNull BACnetDevice device, @NonNull RemoteDevice rd,
                                                        boolean detail, boolean includeError) {
        final ObjectIdentifier objId = rd.getObjectIdentifier();
        final String networkCode = device.protocol().identifier();
        final UUID networkId = networkCache().getDataKey(networkCode).orElse(null);
        return parseRemoteObject(device, rd, objId, detail, includeError).map(pvm -> RemoteDeviceMixin.create(rd, pvm))
                                                                         .map(rdm -> rdm.setNetworkCode(networkCode)
                                                                                        .setNetworkId(networkId));
    }

}
