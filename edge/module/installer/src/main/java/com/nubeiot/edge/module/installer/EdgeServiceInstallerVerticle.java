package com.nubeiot.edge.module.installer;

import java.util.Set;
import java.util.function.Supplier;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.InstallerVerticle;
import com.nubeiot.edge.installer.rule.RuleRepository;
import com.nubeiot.edge.installer.service.AppDeployerDefinition;
import com.nubeiot.edge.installer.service.InstallerService;
import com.nubeiot.edge.module.installer.service.EdgeInstallerService;

import lombok.NonNull;

public final class EdgeServiceInstallerVerticle extends InstallerVerticle<EdgeInstallerService> {

    @Override
    protected Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return ServiceInstallerEntityHandler.class;
    }

    @Override
    protected @NonNull AppDeployerDefinition appDeployerDefinition() {
        return AppDeployerDefinition.create("app");
    }

    @Override
    protected @NonNull RuleRepository ruleRepository() {
        return RuleRepository.createJVMRule("com.nubeiot.edge.connector", "com.nubeiot.edge.rule");
    }

    @Override
    protected Supplier<Set<EdgeInstallerService>> services(@NonNull InstallerEntityHandler handler) {
        return () -> InstallerService.createServices(handler, EdgeInstallerService.class);
    }

}
