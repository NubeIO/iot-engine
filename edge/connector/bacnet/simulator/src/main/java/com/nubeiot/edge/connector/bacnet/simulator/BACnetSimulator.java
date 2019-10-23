package com.nubeiot.edge.connector.bacnet.simulator;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Configs;
import com.nubeiot.edge.connector.bacnet.AbstractBACnetVerticle;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.listener.WhoIsListener;

import lombok.NonNull;

public final class BACnetSimulator extends AbstractBACnetVerticle<SimulatorConfig> {

    @Override
    protected @NonNull Class<SimulatorConfig> bacnetConfigClass() {
        return SimulatorConfig.class;
    }

    @Override
    protected @NonNull Maybe<JsonObject> registerServices(@NonNull SimulatorConfig config) {
        return Maybe.empty();
    }

    @Override
    protected @NonNull Single<List<BACnetNetwork>> findNetworks(@NonNull SimulatorConfig config) {
        JsonObject points = Configs.loadJsonConfig("points.json");
        return Single.just(config.getNetworks().toNetworks());
    }

    @Override
    protected BACnetDevice handle(BACnetDevice device) {
        return device.addListener(new WhoIsListener());
    }

    @Override
    protected Future<Void> stopBACnet() {
        return Future.succeededFuture();
    }

}
