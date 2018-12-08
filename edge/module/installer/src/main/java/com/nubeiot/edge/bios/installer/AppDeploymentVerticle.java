package com.nubeiot.edge.bios.installer;

import java.util.function.Supplier;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleTypeRule;

import io.reactivex.Single;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public final class AppDeploymentVerticle extends EdgeVerticle {

    @Override
    protected void registerEventBus() {
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

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new InstallerModuleTypeRuleProvider();
    }

}
