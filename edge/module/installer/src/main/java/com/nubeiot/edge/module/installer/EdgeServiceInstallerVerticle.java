package com.nubeiot.edge.module.installer;

import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.type.EventMessageService;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.ModuleEventListener;
import com.nubeiot.edge.core.TransactionEventListener;
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

    @SuppressWarnings("unchecked")
    private void publishService(MicroContext microContext) {
        Functions.getIfThrow(microContext::getLocalController).ifPresent(discovery -> {
            Record r1 = EventMessageService.createRecord("bios.installer.service",
                                                         EdgeInstallerEventBus.getServiceInstaller(true).getAddress(),
                                                         EventMethodDefinition.createDefault("/services",
                                                                                             "/:service_id"));
            Record r2 = EventMessageService.createRecord("bios.installer.service.transaction",
                                                         EdgeInstallerEventBus.getServiceTransaction(true).getAddress(),
                                                         EventMethodDefinition.createDefault("/services/transactions",
                                                                                             "/:transaction_id"));
            Record r3 = EventMessageService.createRecord("bios.installer.service.last-transaction",
                                                         EdgeInstallerEventBus.getServiceLastTransaction(true)
                                                                              .getAddress(),
                                                         EventMethodDefinition.createDefault(
                                                             "/services/:module_id/transactions", "/:transaction_id"));
            Single.concatArray(discovery.addRecord(r1), discovery.addRecord(r2), discovery.addRecord(r3)).subscribe();
        });
    }

    @Override
    public void registerEventbus(EventController controller) {
        boolean local = this.nubeConfig.getAppConfig().toJson().getBoolean("deviceLocal", false);
        controller.register(EdgeInstallerEventBus.SERVICE_DEPLOYMENT, new ModuleLoader(vertx))
                  .register(EdgeInstallerEventBus.getServiceInstaller(local),
                            new ModuleEventListener(this, EdgeInstallerEventBus.getServiceInstaller(local)))
                  .register(EdgeInstallerEventBus.getServiceTransaction(local),
                            new TransactionEventListener(this, EdgeInstallerEventBus.getServiceTransaction(local)))
                  .register(EdgeInstallerEventBus.getServiceLastTransaction(local),
                            new LastTransactionEventListener(this,
                                                             EdgeInstallerEventBus.getServiceLastTransaction(local)));
    }

}
