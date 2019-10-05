package com.nubeiot.edge.bios;

import java.util.Set;
import java.util.function.Supplier;

import com.nubeiot.edge.bios.service.BiosInstallerService;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.service.DeployerDefinition;
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
    protected Supplier<Set<? extends InstallerService>> services() {
        return () -> InstallerService.createServices(getEntityHandler(), BiosInstallerService.class);
    }

    @Override
    protected @NonNull DeployerDefinition deploymentService() {
        return DeployerDefinition.createDefault(InstallerEventModel.BIOS_DEPLOYMENT,
                                                InstallerEventModel.BIOS_POST_DEPLOYMENT, getEntityHandler());
    }

}
