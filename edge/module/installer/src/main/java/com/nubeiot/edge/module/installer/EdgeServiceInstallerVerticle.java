package com.nubeiot.edge.module.installer;

import java.util.Set;
import java.util.function.Supplier;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.InstallerVerticle;
import com.nubeiot.edge.installer.loader.ModuleTypeRule;
import com.nubeiot.edge.installer.service.AppDeployerDefinition;
import com.nubeiot.edge.installer.service.InstallerService;
import com.nubeiot.edge.module.installer.service.EdgeInstallerService;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.NonNull;

public final class EdgeServiceInstallerVerticle extends InstallerVerticle<EdgeInstallerService> {

    @Override
    protected Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return ServiceInstallerEntityHandler.class;
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new ServiceInstallerRuleProvider();
    }

    @Override
    protected @NonNull AppDeployerDefinition appDeployerDefinition() {
        return AppDeployerDefinition.create(InstallerEventModel.SERVICE_DEPLOYMENT,
                                            InstallerEventModel.SERVICE_DEPLOYMENT_TRACKER,
                                            InstallerEventModel.SERVICE_DEPLOYMENT_FINISHER);
    }

    @Override
    protected Supplier<Set<EdgeInstallerService>> services(@NonNull InstallerEntityHandler handler) {
        return () -> InstallerService.createServices(handler, EdgeInstallerService.class);
    }

}
