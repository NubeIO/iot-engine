package com.nubeiot.edge.bios;

import java.util.function.Supplier;

import com.nubeiot.core.event.EventController;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventListener;
import com.nubeiot.edge.core.TransactionEventListener;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

public class EdgeBiosVerticle extends EdgeVerticle {

    static final String SHARED_MODULE_RULE = "MODULE_RULE";

    @Override
    public void start() {
        super.start();
        this.addSharedData(SHARED_MODULE_RULE, this.getModuleRule());
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new EdgeBiosRuleProvider();
    }

    @Override
    protected Class<? extends EdgeEntityHandler> entityHandlerClass() {
        return EdgeBiosEntityHandler.class;
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(EdgeInstallerEventBus.BIOS_INSTALLER,
                            new ModuleEventListener(this, EdgeInstallerEventBus.BIOS_INSTALLER));
        controller.register(EdgeInstallerEventBus.BIOS_TRANSACTION,
                            new TransactionEventListener(this, EdgeInstallerEventBus.BIOS_TRANSACTION));
        controller.register(EdgeInstallerEventBus.BIOS_DEPLOYMENT, new ModuleLoader(vertx));
    }

}
