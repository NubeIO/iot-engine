package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.DiscoverLevel;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.Fields;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.discover.RemoteDeviceScanner;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.nubeiot.edge.connector.bacnet.translator.BACnetDeviceTranslator;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

public final class DeviceDiscovery extends AbstractDiscoveryService implements BACnetDiscoveryService {

    DeviceDiscovery(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull String servicePath() {
        return "/network/:" + Fields.networkCode + "/device";
    }

    @Override
    public String paramPath() {
        return Fields.deviceCode;
    }

    @Override
    public Single<JsonObject> list(RequestData reqData) {
        final DiscoverRequest request = DiscoverRequest.from(reqData, DiscoverLevel.NETWORK);
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        final CommunicationProtocol requestProtocol = parseNetworkProtocol(request);
        logger.info("Discover devices by network {}", requestProtocol.toJson());
        final BACnetDevice device = getDeviceCache().get(requestProtocol);
        return device.scanRemoteDevices(options)
                     .map(RemoteDeviceScanner::getRemoteDevices)
                     .flattenAsObservable(r -> r)
                     .flatMapSingle(rd -> parseRemoteDevice(device, rd, options.isDetail(), false))
                     .toList()
                     .map(results -> DiscoverResponse.builder().remoteDevices(results).build().toJson())
                     .doFinally(device::stop);
    }

    @Override
    public Single<JsonObject> get(RequestData reqData) {
        return doGet(reqData, (device, integer) -> device).map(RemoteDeviceMixin::toJson);
    }

    @Override
    public Single<JsonObject> discoverThenDoBatch(RequestData reqData) {
        return doBatch(reqData.body());
    }

    @Override
    public Single<JsonObject> discoverThenDoPersist(RequestData reqData) {
        return doGet(reqData, this::validateCache).filter(rd -> Objects.nonNull(rd.getNetworkId()))
                                                  .switchIfEmpty(Single.error(new NotFoundException(
                                                      "Not found network information. Need to persist network first")))
                                                  .flatMap(this::doPersist);
    }

    @Override
    protected String parseResourceId(JsonObject resource) {
        return resource.getJsonObject("device", new JsonObject()).getString("id");
    }

    @Override
    public @NonNull EntityMetadata context() {
        return DeviceMetadata.INSTANCE;
    }

    private Single<JsonObject> doPersist(@NonNull RemoteDeviceMixin rd) {
        final BACnetNetworkCache networkCache = getNetworkCache();
        final BACnetDeviceCache deviceCache = getDeviceCache();
        final CommunicationProtocol protocol = networkCache.get(rd.getNetworkCode());
        return doPersist(JsonPojo.from(new BACnetDeviceTranslator().serialize(rd)).toJson()).map(response -> {
            deviceCache.addDataKey(protocol, rd.getInstanceNumber(), parsePersistResponse(response));
            return response;
        });
    }

    private BACnetDevice validateCache(@NonNull BACnetDevice device, @NonNull Integer deviceCode) {
        final Optional<UUID> deviceId = getDeviceCache().getDataKey(device.protocol(), deviceCode);
        if (deviceId.isPresent()) {
            throw new AlreadyExistException(
                "Already existed device " + deviceCode + " in network code " + device.protocol().identifier());
        }
        return device;
    }

    private Single<RemoteDeviceMixin> doGet(@NonNull RequestData reqData,
                                            @NonNull BiFunction<BACnetDevice, Integer, BACnetDevice> validation) {
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        final DiscoverRequest request = DiscoverRequest.from(reqData, DiscoverLevel.DEVICE);
        final CommunicationProtocol protocol = parseNetworkProtocol(request);
        logger.info("Discover device {} by network {}", request.getDeviceCode(), protocol.toJson());
        final BACnetDevice device = getDeviceCache().get(protocol);
        return validation.apply(device, request.getDeviceCode())
                         .discoverRemoteDevice(request.getDeviceCode(), options)
                         .flatMap(rd -> parseRemoteDevice(device, rd, true, options.isDetail()))
                         .doFinally(device::stop);
    }

    private Single<RemoteDeviceMixin> parseRemoteDevice(@NonNull BACnetDevice device, @NonNull RemoteDevice rd,
                                                        boolean detail, boolean includeError) {
        final ObjectIdentifier objId = rd.getObjectIdentifier();
        final LocalDevice ld = device.localDevice();
        final String networkCode = device.protocol().identifier();
        final UUID networkId = getNetworkCache().getDataKey(networkCode).orElse(null);
        return parseRemoteObject(ld, rd, objId, detail, includeError).map(pvm -> RemoteDeviceMixin.create(rd, pvm))
                                                                     .map(rdm -> rdm.setNetworkCode(networkCode)
                                                                                    .setNetworkId(networkId));
    }

}
