package com.nubeiot.edge.module.installer;

import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.micro.type.EventMessageService;
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
        addProvider(new MicroserviceProvider(), this::publishService);
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
        final ServiceDiscoveryController discovery = microContext.getLocalController();
        if (!discovery.isEnabled()) {
            return;
        }
        Record r1 = EventMessageService.createRecord("bios.installer.service",
                                                     InstallerEventModel.getServiceInstaller(true).getAddress(),
                                                     EventMethodDefinition.createDefault("/services", "/:service_id"));
        Record r2 = EventMessageService.createRecord("bios.installer.service.transaction",
                                                     InstallerEventModel.getServiceTransaction(true).getAddress(),
                                                     EventMethodDefinition.createDefault("/services/transactions",
                                                                                         "/:transaction_id"));
        Record r3 = EventMessageService.createRecord("bios.installer.service.last-transaction",
                                                     InstallerEventModel.getServiceLastTransaction(true).getAddress(),
                                                     EventMethodDefinition.createDefault(
                                                         "/services/:module_id/transactions", "/:transaction_id"));
        Single.concatArray(discovery.addRecord(r1), discovery.addRecord(r2), discovery.addRecord(r3)).subscribe();
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        final EventModel serviceInstaller = InstallerEventModel.getServiceInstaller(true);
        final EventModel serviceTransaction = InstallerEventModel.getServiceTransaction(true);
        final EventModel serviceLastTransaction = InstallerEventModel.getServiceLastTransaction(true);
        eventClient.register(InstallerEventModel.SERVICE_DEPLOYMENT, new ModuleLoader(vertx))
                   .register(serviceInstaller, new ModuleEventListener(this, serviceInstaller))
                   .register(serviceTransaction, new TransactionEventListener(this, serviceTransaction))
                   .register(serviceLastTransaction, new LastTransactionEventListener(this, serviceLastTransaction));
    }

}
