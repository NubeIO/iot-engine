package com.nubeiot.edge.bios.installer;

import java.util.function.Supplier;

import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.EdgeEventBus;

import io.reactivex.Single;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public final class AppDeploymentVerticle extends EdgeVerticle {

    @Override
    protected void registerEventBus() {
        final EventBus bus = getVertx().eventBus();
        bus.consumer(EdgeEventBus.APP_INSTALLER.getAddress(),
                     m -> new ModuleEventHandler(this, EdgeEventBus.APP_INSTALLER).handleMessage(m));
        bus.consumer(EdgeEventBus.APP_TRANSACTION.getAddress(),
                     m -> new TransactionEventHandler(this, EdgeEventBus.APP_TRANSACTION).handleMessage(m));
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
