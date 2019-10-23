package com.nubeiot.edge.bios.mock;

import com.nubeiot.core.event.EventListener;
import com.nubeiot.edge.bios.EdgeBiosVerticle;
import com.nubeiot.edge.bios.loader.DeploymentAsserter;
import com.nubeiot.edge.bios.loader.MockAppDeploymentService;
import com.nubeiot.edge.installer.service.AppDeployer;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockEdgeBiosVerticle extends EdgeBiosVerticle {

    private final DeploymentAsserter deploymentAsserter;
    private final boolean deployState;

    public MockEdgeBiosVerticle(DeploymentAsserter deploymentAsserter) {
        this(deploymentAsserter, true);
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

    @Override
    protected @lombok.NonNull AppDeployer appDeployer() {
        final EventListener loaderHandler = new MockAppDeploymentService(this, deploymentAsserter, deployState);
        return MockAppDeployer.create(InstallerEventModel.BIOS_DEPLOYMENT, InstallerEventModel.BIOS_DEPLOYMENT_TRACKER,
                                      InstallerEventModel.BIOS_DEPLOYMENT_FINISHER, loaderHandler);
    }

}
