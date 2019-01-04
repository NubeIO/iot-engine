package com.nubeiot.edge.bios;

import java.util.function.Supplier;

import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.EdgeEventBus;

public final class EdgeBiosVerticle extends EdgeVerticle {

    @Override
    protected EventController registerEventBus(EventController controller) {
        controller.consume(EdgeEventBus.BIOS_INSTALLER, new ModuleEventHandler(this, EdgeEventBus.BIOS_INSTALLER));
        controller.consume(EdgeEventBus.BIOS_TRANSACTION,
                           new TransactionEventHandler(this, EdgeEventBus.BIOS_TRANSACTION));
        return controller;
    }

    @Override
    protected Class<? extends EdgeEntityHandler> entityHandlerClass() {
        return EdgeBiosEntityHandler.class;
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new EdgeBiosRuleProvider();
    }

}
