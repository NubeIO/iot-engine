package com.nubeiot.edge.bios.installer;

import java.util.function.Supplier;

import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.EdgeEventBus;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public final class AppDeploymentVerticle extends EdgeVerticle {

    @Override
    protected void registerEventBus() {
        EventController controller = new EventController(getVertx());
        controller.consume(EdgeEventBus.APP_INSTALLER, new ModuleEventHandler(this, EdgeEventBus.APP_INSTALLER));
        controller.consume(EdgeEventBus.APP_TRANSACTION,
                           new TransactionEventHandler(this, EdgeEventBus.APP_TRANSACTION));
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
