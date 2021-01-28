package com.nubeiot.edge.connector.bacnet.service.discovery;

import java.util.Optional;
import java.util.UUID;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.exceptions.AlreadyExistException;
import io.github.zero88.qwe.exceptions.NotFoundException;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryParams;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryRequest;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryResponse;
import com.nubeiot.edge.connector.bacnet.entity.BACnetDeviceEntity;
import com.nubeiot.edge.connector.bacnet.internal.request.RemoteDeviceScanner;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class BACnetDeviceExplorer extends AbstractBACnetExplorer<BACnetDeviceEntity>
    implements BACnetExplorer<BACnetDeviceEntity> {

    BACnetDeviceExplorer(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy);
    }

    @Override
    public @NonNull Class<BACnetDeviceEntity> context() {
        return BACnetDeviceEntity.class;
    }

    @Override
    public @NonNull String servicePath() {
        return "/network/:" + DiscoveryParams.Fields.networkCode + "/device";
    }

    @Override
    public String paramPath() {
        return DiscoveryParams.Fields.deviceInstance;
    }

    @Override
    public Single<JsonObject> discover(RequestData reqData) {
        return doGet(createDiscoveryRequest(reqData, DiscoveryLevel.DEVICE)).map(RemoteDeviceMixin::toJson);
    }

    @Override
    public Single<JsonObject> discoverMany(RequestData reqData) {
        final DiscoveryRequest request = createDiscoveryRequest(reqData, DiscoveryLevel.NETWORK);
        final BACnetDevice device = getBACnetDeviceFromCache(request);
        log.info("Discovering devices in network {}...", device.protocol().identifier());
        return device.scanRemoteDevices(request.options())
                     .map(RemoteDeviceScanner::getRemoteDevices)
                     .flattenAsObservable(r -> r)
                     .flatMapSingle(rd -> parseRemoteDevice(device, rd, request.options().isDetail(), false))
                     .toList()
                     .map(results -> DiscoveryResponse.builder().remoteDevices(results).build().toJson())
                     .doFinally(device::stop);
    }

    @Override
    protected String parseResourceId(JsonObject resource) {
        return resource.getJsonObject("device", new JsonObject()).getString("id");
    }

    private Single<RemoteDeviceMixin> doGet(@NonNull DiscoveryRequest request) {
        final BACnetDevice device = getBACnetDeviceFromCache(request);
        log.info("Discovering remote device {} in network {}...",
                 ObjectIdentifierMixin.serialize(request.remoteDeviceId()), device.protocol().identifier());
        return device.discoverRemoteDevice(request.remoteDeviceId(), request.options())
                     .flatMap(rd -> parseRemoteDevice(device, rd, true, request.options().isDetail()))
                     .doFinally(device::stop);
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

    private DiscoveryRequest validateCache(@NonNull DiscoveryRequest request) {
        final BACnetDevice device = getBACnetDeviceFromCache(request);
        networkCache().getDataKey(device.protocol().identifier())
                      .orElseThrow(() -> new NotFoundException(
                          "Not found a persistence network by network code " + device.protocol().identifier()));
        final Optional<UUID> deviceId = deviceCache().getDataKey(device.protocol(), request.remoteDeviceId());
        if (deviceId.isPresent()) {
            throw new AlreadyExistException(
                "Already existed device " + ObjectIdentifierMixin.serialize(request.remoteDeviceId()) + " in network " +
                device.protocol().identifier());
        }
        return request;
    }

}
