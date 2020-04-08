package com.nubeiot.edge.bios;

import java.util.Set;
import java.util.function.Supplier;

import com.nubeiot.edge.bios.service.BiosInstallerService;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.InstallerVerticle;
import com.nubeiot.edge.installer.loader.ModuleTypeRule;
import com.nubeiot.edge.installer.service.AppDeployerDefinition;
import com.nubeiot.edge.installer.service.InstallerService;

import lombok.NonNull;

public class EdgeBiosVerticle extends InstallerVerticle<BiosInstallerService> {

    @Override
    protected Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return EdgeBiosEntityHandler.class;
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new EdgeBiosRuleProvider();
    }

    @Override
    protected @NonNull AppDeployerDefinition appDeployerDefinition() {
        return AppDeployerDefinition.create("bios");
    }

    @Override
    protected Supplier<Set<BiosInstallerService>> services(@NonNull InstallerEntityHandler handler) {
        return () -> InstallerService.createServices(handler, BiosInstallerService.class);
    }

}
