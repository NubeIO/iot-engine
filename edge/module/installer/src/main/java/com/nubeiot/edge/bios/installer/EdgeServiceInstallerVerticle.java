package com.nubeiot.edge.bios.installer;

import java.util.function.Supplier;

import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

public final class EdgeServiceInstallerVerticle extends EdgeVerticle {

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(EdgeInstallerEventBus.SERVICE_INSTALLER,
                            new ModuleEventHandler(this, EdgeInstallerEventBus.SERVICE_INSTALLER));
        controller.register(EdgeInstallerEventBus.SERVICE_TRANSACTION,
                            new TransactionEventHandler(this, EdgeInstallerEventBus.SERVICE_TRANSACTION));
        controller.register(EdgeInstallerEventBus.SERVICE_DEPLOYMENT, new ModuleLoader(vertx));
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new ServiceInstallerRuleProvider();
    }

    @Override
    protected Class<? extends EdgeEntityHandler> entityHandlerClass() {
        return ServiceInstallerEntityHandler.class;
    }

}
