package com.nubeiot.edge.connector.bacnet.service.discover;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.converter.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.dto.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;

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
        return null;
    }

    @EventContractor(action = EventAction.DISCOVER, returnType = Single.class)
    public Single<JsonObject> discover(RequestData reqData) {
        final BACnetNetwork network = BACnetNetwork.factory(reqData.body());
        logger.info("Request network {}", network.toJson());
        final BACnetDevice device = new BACnetDevice(getVertx(), getSharedKey(), network);
        final LocalDeviceMetadata metadata = getSharedDataValue(BACnetDevice.EDGE_BACNET_METADATA);
        final DiscoverOptions options = DiscoverOptions.from(metadata.getMaxTimeoutInMS(), reqData);
        return device.discoverRemoteDevices(options)
                     .map(remoteDevice -> options.isDetail()
                                          ? BACnetDataConversions.deviceExtended(remoteDevice)
                                          : BACnetDataConversions.deviceMinimal(remoteDevice))
                     .collect(JsonArray::new, JsonArray::add)
                     .map(results -> new JsonObject().put("remote_devices", results));
    }

}
