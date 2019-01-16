package com.nubeiot.edge.bios.installer;

import java.util.function.Supplier;

import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.EdgeEventBus;

public final class EdgeInstallerVerticle extends EdgeVerticle {

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(EdgeEventBus.APP_INSTALLER, new ModuleEventHandler(this, EdgeEventBus.APP_INSTALLER));
        controller.register(EdgeEventBus.APP_TRANSACTION,
                            new TransactionEventHandler(this, EdgeEventBus.APP_TRANSACTION));
        controller.register(EdgeEventBus.APP_DEPLOYMENT, new ModuleLoader(vertx));
    }

    @Override
    protected Class<? extends EdgeEntityHandler> entityHandlerClass() {
        return EdgeInstallerEntityHandler.class;
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new EdgeInstallerRuleProvider();
    }

}
