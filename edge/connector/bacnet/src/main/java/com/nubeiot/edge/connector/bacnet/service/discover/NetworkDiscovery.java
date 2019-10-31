package com.nubeiot.edge.connector.bacnet.service.discover;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.cache.IpNetworkCache;
import com.nubeiot.edge.connector.bacnet.converter.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;

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
        return "network_name";
    }

    @EventContractor(action = EventAction.GET_LIST, returnType = Single.class)
    public Single<JsonObject> list(RequestData reqData) {
        final IpNetworkCache cache = getSharedDataValue(BACnetCacheInitializer.EDGE_NETWORK_CACHE);
        return Observable.fromIterable(cache.all().entrySet())
                         .collect(JsonObject::new, (json, net) -> json.put(net.getKey(), net.getValue().toJson()))
                         .map(json -> new JsonObject().put("ipv4", json));
    }

    @EventContractor(action = EventAction.DISCOVER, returnType = Single.class)
    public Single<JsonObject> discover(RequestData reqData) {
        final BACnetNetwork network = BACnetNetwork.factory(reqData.body());
        logger.info("Request network {}", network.toJson());
        final BACnetDeviceCache cache = getSharedDataValue(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
        final BACnetDevice device = new BACnetDevice(getVertx(), getSharedKey(), network);
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        return device.discoverRemoteDevices(options)
                     .map(remoteDevice -> options.isDetail()
                                          ? BACnetDataConversions.deviceExtended(remoteDevice)
                                          : BACnetDataConversions.deviceMinimal(remoteDevice))
                     .collect(JsonArray::new, JsonArray::add)
                     .map(results -> new JsonObject().put("remote_devices", results))
                     .doFinally(device::stop);
    }

}
