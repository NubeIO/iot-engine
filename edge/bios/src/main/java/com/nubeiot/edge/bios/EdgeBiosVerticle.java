package com.nubeiot.edge.bios;

import java.util.Set;
import java.util.function.Supplier;

import com.nubeiot.edge.bios.service.BiosInstallerService;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.service.AppDeployer;
import com.nubeiot.edge.core.service.InstallerService;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.NonNull;

public class EdgeBiosVerticle extends InstallerVerticle {

    @Override
    protected Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return EdgeBiosEntityHandler.class;
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new EdgeBiosRuleProvider();
    }

    @Override
    protected @NonNull AppDeployer appDeployer() {
        return AppDeployer.create(InstallerEventModel.BIOS_DEPLOYMENT, InstallerEventModel.BIOS_DEPLOYMENT_TRACKER,
                                  InstallerEventModel.BIOS_DEPLOYMENT_FINISHER);
    }

    @Override
    protected Supplier<Set<? extends InstallerService>> services(@NonNull InstallerEntityHandler handler) {
        return () -> InstallerService.createServices(handler, BiosInstallerService.class);
    }

}
