package com.nubeiot.edge.connector.bacnet.simulator;

import java.util.List;
import java.util.Optional;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.utils.Configs;
import com.nubeiot.edge.connector.bacnet.AbstractBACnetVerticle;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.handler.DiscoverCompletionHandler;
import com.nubeiot.edge.connector.bacnet.listener.WhoIsListener;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class BACnetSimulator extends AbstractBACnetVerticle<SimulatorConfig> {

    private final DiscoverCompletionHandler handler;

    public BACnetSimulator() {
        handler = null;
    }

    @Override
    @NonNull
    protected Class<SimulatorConfig> bacnetConfigClass() {
        return SimulatorConfig.class;
    }

    @Override
    @NonNull
    protected Single<JsonObject> registerApis(@NonNull EventbusClient client, @NonNull SimulatorConfig config) {
        return Single.just(new JsonObject().put("message", "No BACnet services")).doOnSuccess(logger::info);
    }

    @Override
    @NonNull
    protected Single<JsonObject> registerSubscriber(@NonNull EventbusClient client, @NonNull SimulatorConfig config) {
        return Single.just(new JsonObject().put("message", "No BACnet Subscriber")).doOnSuccess(logger::info);
    }

    @Override
    protected @NonNull Single<List<CommunicationProtocol>> availableNetworks(@NonNull SimulatorConfig config) {
        JsonObject points = Configs.loadJsonConfig("points.json");
        return Single.just(config.getNetworks().toNetworks())
                     .flattenAsObservable(networks -> networks)
                     .map(BACnetNetwork::toProtocol)
                     .toList();
    }

    @Override
    protected void addListenerOnEachDevice(BACnetDevice device) {
        device.addListeners(new WhoIsListener());
    }

    @Override
    protected Future<Void> stopBACnet() {
        return Future.succeededFuture();
    }

    @Override
    protected DiscoverCompletionHandler createDiscoverCompletionHandler() {
        return Optional.ofNullable(handler).orElse(super.createDiscoverCompletionHandler());
    }

}
