package com.nubeiot.edge.bios;

import java.util.function.Supplier;

import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.EdgeEventBus;

public final class EdgeBiosVerticle extends EdgeVerticle {

    static final String SHARED_MODULE_RULE = "MODULE_RULE";

    @Override
    public void start() {
        super.start();
        this.addSharedData(SHARED_MODULE_RULE, this.getModuleRule());
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(EdgeEventBus.BIOS_INSTALLER, new ModuleEventHandler(this, EdgeEventBus.BIOS_INSTALLER));
        controller.register(EdgeEventBus.BIOS_TRANSACTION,
                            new TransactionEventHandler(this, EdgeEventBus.BIOS_TRANSACTION));
        controller.register(EdgeEventBus.BIOS_DEPLOYMENT, new ModuleLoader(vertx));
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
