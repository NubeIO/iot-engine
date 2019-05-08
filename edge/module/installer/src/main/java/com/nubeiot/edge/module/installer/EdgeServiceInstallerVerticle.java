package com.nubeiot.edge.module.installer;

import java.util.function.Supplier;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventListener;
import com.nubeiot.edge.core.TransactionEventListener;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

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
                                              InstallerEventModel.getServiceInstaller(true).getAddress(),
                                              EventMethodDefinition.createDefault("/services", "/:service_id"))
                       .subscribe();
        localController.addEventMessageRecord("service_transaction",
                                              InstallerEventModel.getServiceTransaction(true).getAddress(),
                                              EventMethodDefinition.createDefault("/services/transactions",
                                                                                  "/:transaction_id")).subscribe();
        localController.addEventMessageRecord("service_last_transaction",
                                              InstallerEventModel.getServiceLastTransaction(true).getAddress(),
                                              EventMethodDefinition.createDefault("/services/:module_id/transactions",
                                                                                  "/:transaction_id")).subscribe();
    }

    @Override
    public void registerEventbus(EventController controller) {
        boolean local = this.nubeConfig.getAppConfig().toJson().getBoolean("deviceLocal", false);
        controller.register(InstallerEventModel.getServiceInstaller(local),
                            new ModuleEventListener(this, InstallerEventModel.getServiceInstaller(local)));
        controller.register(InstallerEventModel.getServiceTransaction(local),
                            new TransactionEventListener(this, InstallerEventModel.getServiceTransaction(local)));
        controller.register(InstallerEventModel.getServiceLastTransaction(local),
                            new LastTransactionEventListener(this,
                                                             InstallerEventModel.getServiceLastTransaction(local)));
        controller.register(InstallerEventModel.SERVICE_DEPLOYMENT, new ModuleLoader(vertx));
    }

}
