package com.nubeiot.edge.module.installer;

import java.util.Set;
import java.util.function.Supplier;

import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.core.service.DeployerDefinition;
import com.nubeiot.edge.core.service.InstallerService;
import com.nubeiot.edge.module.installer.service.EdgeInstallerService;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.NonNull;

public final class EdgeServiceInstallerVerticle extends InstallerVerticle {

    @Override
    protected Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return ServiceInstallerEntityHandler.class;
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new ServiceInstallerRuleProvider();
    }

    @Override
    protected Supplier<Set<? extends InstallerService>> services() {
        return () -> InstallerService.createServices(getEntityHandler(), EdgeInstallerService.class);
    }

    @Override
    protected @NonNull DeployerDefinition deploymentService() {
        return DeployerDefinition.createDefault(InstallerEventModel.SERVICE_DEPLOYMENT,
                                                InstallerEventModel.SERVICE_POST_DEPLOYMENT, getEntityHandler());
    }

}
