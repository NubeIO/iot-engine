package com.nubeiot.edge.bios.startupmodules.mock;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.edge.bios.EdgeBiosVerticle;
import com.nubeiot.edge.bios.loader.MockAppDeploymentService;
import com.nubeiot.edge.bios.mock.MockAppDeployer;
import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.AppDeployer;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

public class MockBiosStartupModulesVerticle extends EdgeBiosVerticle {

    private final Class<? extends InstallerEntityHandler> entityHandlerClass;

    public MockBiosStartupModulesVerticle(Class<? extends InstallerEntityHandler> entityHandlerClass) {
        this.entityHandlerClass = entityHandlerClass;
    }

    @Override
    protected Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return this.entityHandlerClass;
    }

    @Override
    protected @lombok.NonNull AppDeployer appDeployer() {
        final EventListener loaderHandler = new MockAppDeploymentService(this, null, true);
        return MockAppDeployer.create(InstallerEventModel.BIOS_DEPLOYMENT, InstallerEventModel.BIOS_DEPLOYMENT_TRACKER,
                                      InstallerEventModel.BIOS_DEPLOYMENT_FINISHER, loaderHandler);
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
