package com.nubeiot.edge.bios.installer;

import java.util.function.Supplier;

import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.EdgeEventBus;

public final class EdgeInstallerVerticle extends EdgeVerticle {

    @Override
    protected EventController registerEventBus(EventController controller) {
        controller.consume(EdgeEventBus.APP_INSTALLER, new ModuleEventHandler(this, EdgeEventBus.APP_INSTALLER));
        controller.consume(EdgeEventBus.APP_TRANSACTION,
                           new TransactionEventHandler(this, EdgeEventBus.APP_TRANSACTION));
        return controller;
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
