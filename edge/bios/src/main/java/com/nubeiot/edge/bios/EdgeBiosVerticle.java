package com.nubeiot.edge.bios;

import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventController;
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

public class EdgeBiosVerticle extends EdgeVerticle {

    static final String SHARED_MODULE_RULE = "MODULE_RULE";

    @Override
    public void start() {
        super.start();
        this.addSharedData(SHARED_MODULE_RULE, this.getModuleRule())
            .addProvider(new MicroserviceProvider(), this::publishService);
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
    public void registerEventbus(EventController eventClient) {
        eventClient.register(InstallerEventModel.BIOS_DEPLOYMENT, new ModuleLoader(vertx))
                   .register(InstallerEventModel.BIOS_INSTALLER,
                            new ModuleEventListener(this, InstallerEventModel.BIOS_INSTALLER))
                   .register(InstallerEventModel.BIOS_TRANSACTION,
                             new TransactionEventListener(this, InstallerEventModel.BIOS_TRANSACTION));
    }

    @SuppressWarnings("unchecked")
    private void publishService(MicroContext microContext) {
        final ServiceDiscoveryController discovery = microContext.getLocalController();
        if (!discovery.isEnabled()) {
            return;
        }
        final Record r1 = EventMessageService.createRecord("bios.installer",
                                                           InstallerEventModel.BIOS_INSTALLER.getAddress(),
                                                           EventMethodDefinition.createDefault("/modules",
                                                                                               "/:module_id"));
        final Record r2 = EventMessageService.createRecord("bios.installer.transaction",
                                                           InstallerEventModel.BIOS_TRANSACTION.getAddress(),
                                                           EventMethodDefinition.createDefault("/modules/transactions",
                                                                                               "/:transaction_id"));
        Single.concatArray(discovery.addRecord(r1), discovery.addRecord(r2)).subscribe();
    }

}
