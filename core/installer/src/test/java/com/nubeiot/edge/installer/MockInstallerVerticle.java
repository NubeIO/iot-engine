package com.nubeiot.edge.installer;

import java.util.Set;
import java.util.function.Supplier;

import com.nubeiot.edge.installer.loader.ModuleTypeRule;
import com.nubeiot.edge.installer.service.AppDeployer;
import com.nubeiot.edge.installer.service.InstallerService;

import lombok.NonNull;

public class MockInstallerVerticle extends InstallerVerticle {

    @Override
    protected @NonNull Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return null;
    }

    @Override
    protected @NonNull Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return null;
    }

    @Override
    protected @NonNull AppDeployer appDeployer() {
        return null;
    }

    @Override
    protected @NonNull Supplier<Set<? extends InstallerService>> services(@NonNull InstallerEntityHandler handler) {
        return null;
    }

}
