package com.nubeiot.edge.bios;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.edge.bios.loader.DeploymentAsserter;
import com.nubeiot.edge.bios.loader.MockFailedModuleLoader;
import com.nubeiot.edge.bios.loader.MockModuleLoader;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.service.AppDeployer;
import com.nubeiot.edge.core.service.AppDeploymentTracker;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockBiosEdgeVerticle extends EdgeBiosVerticle {

    private final DeploymentAsserter deploymentAsserter;
    private final boolean failed;

    MockBiosEdgeVerticle(DeploymentAsserter deploymentAsserter) {
        this(deploymentAsserter, false);
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

    @Override
    protected @lombok.NonNull AppDeployer appDeployer(InstallerEntityHandler entityHandler) {
        final EventListener loaderHandler = failed
                                            ? new MockFailedModuleLoader(deploymentAsserter)
                                            : new MockModuleLoader(deploymentAsserter);
        return AppDeployer.create(InstallerEventModel.BIOS_DEPLOYMENT, InstallerEventModel.BIOS_POST_DEPLOYMENT,
                                  loaderHandler, new AppDeploymentTracker(entityHandler));
    }

}
