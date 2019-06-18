package com.nubeiot.edge.module.installer;

import java.util.function.Supplier;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

public final class EdgeServiceInstallerVerticle extends EdgeVerticle {

    @Override
    public void start() {
        super.start();
        boolean isLocal = this.nubeConfig.getAppConfig().toJson().getBoolean("deviceLocal", false);
        if (isLocal) {
            addProvider(new MicroserviceProvider(), this::publishService);
        }
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new ServiceInstallerRuleProvider();
    }

    @Override
    protected Class<? extends EdgeEntityHandler> entityHandlerClass() {
        return ServiceInstallerEntityHandler.class;
    }

    private void publishService(MicroContext microContext) {
        final ServiceDiscoveryController localController = microContext.getLocalController();
        localController.addEventMessageRecord("service_installer",
                                              EdgeInstallerEventBus.getServiceInstaller(true).getAddress(),
                                              EventMethodDefinition.createDefault("/services", "/services/:service_id"))
                       .subscribe();
        localController.addEventMessageRecord("service_transaction",
                                              EdgeInstallerEventBus.getServiceTransaction(true).getAddress(),
                                              EventMethodDefinition.createDefault("/services/transactions",
                                                                                  "/services/transactions" +
                                                                                  "/:transaction_id"))
                       .subscribe();
        localController.addEventMessageRecord("service_last_transaction",
                                              EdgeInstallerEventBus.getServiceLastTransaction(true).getAddress(),
                                              EventMethodDefinition.createDefault("/services/:module_id/transactions",
                                                                                  "/services/:module_id/transactions" +
                                                                                  "/:transaction_id"), null)
                       .subscribe();
    }

    @Override
    public void registerEventbus(EventController controller) {
        boolean local = this.nubeConfig.getAppConfig().toJson().getBoolean("deviceLocal", false);
        controller.register(EdgeInstallerEventBus.getServiceInstaller(local),
                            new ModuleEventHandler(this, EdgeInstallerEventBus.getServiceInstaller(local)));
        controller.register(EdgeInstallerEventBus.getServiceTransaction(local),
                            new TransactionEventHandler(this, EdgeInstallerEventBus.getServiceTransaction(local)));
        controller.register(EdgeInstallerEventBus.getServiceLastTransaction(local),
                            new LastTransactionEventHandler(this,
                                                            EdgeInstallerEventBus.getServiceLastTransaction(local)));
        controller.register(EdgeInstallerEventBus.SERVICE_DEPLOYMENT, new ModuleLoader(vertx));
    }

}
