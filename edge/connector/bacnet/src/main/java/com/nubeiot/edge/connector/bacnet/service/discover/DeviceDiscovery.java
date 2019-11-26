package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.DiscoverLevel;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.Fields;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.discover.RemoteDeviceScanner;
import com.nubeiot.edge.connector.bacnet.dto.RemoteDeviceMixin;
import com.nubeiot.edge.connector.bacnet.translator.BACnetDeviceTranslator;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.DeviceMetadata;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

public final class DeviceDiscovery extends AbstractBACnetDiscoveryService implements BACnetDiscoveryService {

    DeviceDiscovery(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull String servicePath() {
        return "/discovery/bacnet/network/:" + Fields.networkCode + "/device";
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
        final BACnetDeviceCache cache = getSharedDataValue(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
        final BACnetDevice device = cache.get(requestProtocol);
        return device.scanRemoteDevices(options)
                     .map(RemoteDeviceScanner::getRemoteDevices)
                     .flattenAsObservable(r -> r)
                     .flatMapSingle(rd -> parseRemoteDevice(device.getLocalDevice(), rd, options.isDetail(), false))
                     .collect(ArrayList<RemoteDeviceMixin>::new, List::add)
                     .map(results -> DiscoverResponse.builder().remoteDevices(results).build().toJson())
                     .doFinally(device::stop);
    }

    @Override
    public Single<JsonObject> get(RequestData reqData) {
        return doGet(reqData).map(RemoteDeviceMixin::toJson);
    }

    @Override
    public Single<JsonObject> batchPersist(RequestData reqData) {
        return Single.just(new JsonObject());
    }

    @Override
    public Single<JsonObject> persist(RequestData reqData) {
        return doGet(reqData).map(rd -> new BACnetDeviceTranslator().serialize(rd))
                             .flatMap(device -> doPersist(EventAction.CREATE, device.toJson()));
    }

    @Override
    public String destination() {
        return DataPointIndex.lookupApiName(DeviceMetadata.INSTANCE);
    }

    private Single<RemoteDeviceMixin> parseRemoteDevice(@NonNull LocalDevice ld, @NonNull RemoteDevice rd,
                                                        boolean detail, boolean includeError) {
        final ObjectIdentifier objId = rd.getObjectIdentifier();
        return parseRemoteObject(ld, rd, objId, detail, includeError).map(pvm -> RemoteDeviceMixin.create(rd, pvm));
    }

    private Single<RemoteDeviceMixin> doGet(RequestData reqData) {
        final DiscoverRequest request = DiscoverRequest.from(reqData, DiscoverLevel.DEVICE);
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        final CommunicationProtocol protocol = parseNetworkProtocol(request);
        logger.info("Discover device {} by network {}", request.getDeviceCode(), protocol.toJson());
        final BACnetDeviceCache cache = getSharedDataValue(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
        final BACnetDevice device = cache.get(protocol);
        return device.discoverRemoteDevice(request.getDeviceCode(), options)
                     .flatMap(rd -> parseRemoteDevice(device.getLocalDevice(), rd, true, options.isDetail()))
                     .doFinally(device::stop);
    }

}
