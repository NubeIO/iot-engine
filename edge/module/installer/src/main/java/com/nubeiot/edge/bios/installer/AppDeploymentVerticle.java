package com.nubeiot.edge.bios.installer;

import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.loader.ModuleTypeRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nubeiot.core.event.EventModel;

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
    protected ModuleTypeRule registerModuleRule() {
        return new ModuleTypeRule().registerRule(ModuleType.JAVA, "com.nubeiot.edge.services", this.validateGroup(ModuleType.JAVA));
    }

    @Override
    protected Single<JsonObject> initData() {
        logger.info("Setup NubeIO App Installer with config {}", getAppConfig().encode());
        return this.startupModules();
    }
    
    @Override
    protected List<String> getSupportGroups(ModuleType moduleType) {
        //put module type to support further module
        if (ModuleType.JAVA.equals(moduleType)) {
                return Arrays.asList("com.nubeio.edge.connector", "com.nubeio.edge.rule");
        } else {
                return new ArrayList<String>();
        }
        
    }
    
}
