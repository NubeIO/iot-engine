package com.nubeiot.edge.connector.bacnet.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;
import com.nubeiot.edge.connector.bacnet.TransportProvider;
import com.nubeiot.edge.connector.bacnet.dto.BACnetIP;
import com.nubeiot.edge.connector.bacnet.dto.BACnetMSTP;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.utils.BACnetDataConversions;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.cache.RemoteEntityCachePolicy;
import com.serotonin.bacnet4j.transport.Transport;

import lombok.NonNull;

public final class NetworkDiscovery extends AbstractBACnetDiscoveryService implements BACnetDiscoveryService {

    public NetworkDiscovery(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull String servicePath() {
        return "/discovery/bacnet/network";
    }

    @Override
    public String paramPath() {
        return "/:name";
    }

    @EventContractor(action = EventAction.DISCOVER, returnType = Single.class)
    public Single<JsonObject> discover(RequestData requestData) {
        final BACnetNetwork network = BACnetNetwork.factory(requestData.body());
        if (Objects.isNull(network)) {
            throw new IllegalArgumentException(
                "BACnet network identifier is mandatory. Support discovering BACnet " + BACnetIP.TYPE + " or BACnet " +
                BACnetMSTP.TYPE);
        }
        logger.info("Request network {}", network.toJson());
        final LocalDeviceMetadata metadata = getSharedDataValue(BACnetVerticle.DEVICE_METADATA);
        final long timeout = LocalDeviceMetadata.maxTimeout(
            requestData.getFilter().getLong("timeout", metadata.getDiscoverTimeout()));
        final Transport transport = TransportProvider.byConfig(network).get();
        final LocalDevice localDevice = metadata.decorate(new LocalDevice(metadata.getDeviceNumber(), transport));
        return Single.fromCallable(localDevice::initialize)
                     .map(ld -> ld.startRemoteDeviceDiscovery(rd -> ld.getCachePolicies()
                                                                      .putDevicePolicy(rd.getInstanceNumber(),
                                                                                       RemoteEntityCachePolicy.EXPIRE_1_MINUTE)))
                     .delay(timeout, TimeUnit.SECONDS)
                     .doOnEvent((discoverer, throwable) -> discoverer.stop())
                     .flatMap(discover -> Single.fromCallable(discover::getRemoteDevices))
                     .flattenAsObservable(r -> r)
                     .map(BACnetDataConversions::deviceExtended)
                     .collect(JsonArray::new, JsonArray::add)
                     .map(results -> new JsonObject().put("remote_devices", results));
    }

}
