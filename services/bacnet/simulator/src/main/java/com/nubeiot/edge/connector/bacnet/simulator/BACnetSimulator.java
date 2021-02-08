package com.nubeiot.edge.connector.bacnet.simulator;

import java.util.Optional;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.AbstractBACnetApplication;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.handler.DiscoverCompletionHandler;
import com.nubeiot.edge.connector.bacnet.internal.listener.WhoIsListener;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class BACnetSimulator extends AbstractBACnetApplication<SimulatorConfig> {

    private final DiscoverCompletionHandler handler;

    @Override
    @NonNull
    protected Class<SimulatorConfig> bacnetConfigClass() {
        return SimulatorConfig.class;
    }

    @Override
    @NonNull
    protected Single<JsonObject> registerApis(@NonNull SharedDataLocalProxy sharedData,
                                              @NonNull SimulatorConfig config) {
        return Single.just(new JsonObject().put("message", "No BACnet services"));
    }

    @Override
    protected void addListenerOnEachDevice(@NonNull BACnetDevice device) {
        device.addListeners(new WhoIsListener());
    }

    @Override
    protected DiscoverCompletionHandler createDiscoverCompletionHandler() {
        return Optional.ofNullable(handler).orElse(super.createDiscoverCompletionHandler());
    }

}
