package com.nubeiot.edge.bios.mock;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.edge.bios.EdgeBiosVerticle;
import com.nubeiot.edge.bios.loader.DeploymentAsserter;
import com.nubeiot.edge.bios.loader.MockAppDeploymentService;
import com.nubeiot.edge.bios.loader.MockFailedAppDeploymentService;
import com.nubeiot.edge.installer.service.AppDeployer;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockBiosEdgeVerticle extends EdgeBiosVerticle {

    private final DeploymentAsserter deploymentAsserter;
    private final boolean failed;

    public MockBiosEdgeVerticle(DeploymentAsserter deploymentAsserter) {
        this(deploymentAsserter, false);
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

    @Override
    protected @lombok.NonNull AppDeployer appDeployer() {
        final EventListener loaderHandler = failed
                                            ? new MockFailedAppDeploymentService(deploymentAsserter)
                                            : new MockAppDeploymentService(this, deploymentAsserter);
        return MockAppDeployer.create(InstallerEventModel.BIOS_DEPLOYMENT, InstallerEventModel.BIOS_DEPLOYMENT_TRACKER,
                                      InstallerEventModel.BIOS_DEPLOYMENT_FINISHER, loaderHandler);
    }

}
