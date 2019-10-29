package com.nubeiot.edge.connector.bacnet.service.discover;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.utils.ExecutorHelpers;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.converter.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.dto.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.mixin.NetworkInterfaceWrapper;

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
        final Map<NetworkInterface, InterfaceAddress> activeInterfacesIPv4 = Networks.getActiveInterfacesIPv4();
        return ExecutorHelpers.blocking(getVertx(), Networks::getActiveInterfacesIPv4)
                              .flatMapObservable(map -> Observable.fromIterable(map.entrySet()))
                              .map(entry -> new NetworkInterfaceWrapper(entry.getKey(), entry.getValue()))
                              .collect(JsonObject::new, (json, net) -> json.put(net.getName(), net.toJson()))
                              .map(json -> new JsonObject().put("ip", json));
    }

    @EventContractor(action = EventAction.DISCOVER, returnType = Single.class)
    public Single<JsonObject> discover(RequestData reqData) {
        final BACnetNetwork network = BACnetNetwork.factory(reqData.body());
        logger.info("Request network {}", network.toJson());
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
