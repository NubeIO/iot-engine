package com.nubeiot.edge.bios.startupmodules;

import com.nubeiot.edge.bios.EdgeBiosVerticle;
import com.nubeiot.edge.installer.InstallerEntityHandler;

public class MockBiosStartupModulesVerticle extends EdgeBiosVerticle {

    private final Class<? extends InstallerEntityHandler> entityHandlerClass;

    MockBiosStartupModulesVerticle(Class<? extends InstallerEntityHandler> entityHandlerClass) {
        this.entityHandlerClass = entityHandlerClass;
    }

    @Override
    protected Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return this.entityHandlerClass;
    }

    //    @Override
    //    protected @NonNull AppDeployer appDeployer() {
    //        return AppDeployer.create(InstallerEventModel.BIOS_DEPLOYMENT, InstallerEventModel
    //        .BIOS_DEPLOYMENT_TRACKER,
    //                                  new MockModuleLoader(null), new AppDeploymentTracker(entityHandler));
    //    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
