package io.nubespark;

import com.nubeio.iot.edge.EdgeVerticle;
import com.nubeio.iot.edge.ModuleEventHandler;
import com.nubeio.iot.edge.TransactionEventHandler;
import com.nubeio.iot.share.event.EventModel;

import io.reactivex.Single;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public final class AppDeploymentVerticle extends EdgeVerticle {

    @Override
    protected String getDBName() {
        return "nube-app-installer";
    }

    @Override
    protected void registerEventHandler() {
        final EventBus bus = getVertx().eventBus();
        bus.consumer(EventModel.EDGE_APP_INSTALLER.getAddress(),
                     m -> this.handleEvent(m, new ModuleEventHandler(this, EventModel.EDGE_APP_INSTALLER)));
        bus.consumer(EventModel.EDGE_APP_TRANSACTION.getAddress(),
                     m -> this.handleEvent(m, new TransactionEventHandler(this, EventModel.EDGE_APP_TRANSACTION)));
    }

    @Override
    protected Single<JsonObject> initData() {
        logger.info("Setup NubeIO App Installer with config {}", getAppConfig().encode());
        return this.startupModules();
    }

}
