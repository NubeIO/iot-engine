package com.nubeiot.edge.bios;

import com.nubeiot.edge.bios.loader.DeploymentAsserter;

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

    //    @Override
    //    protected @lombok.NonNull AppDeployer appDeployer() {
    //        final EventListener loaderHandler = failed
    //                                            ? new MockFailedModuleLoader(deploymentAsserter)
    //                                            : new MockModuleLoader(deploymentAsserter);
    //        return AppDeployer.create(InstallerEventModel.BIOS_DEPLOYMENT, InstallerEventModel
    //        .BIOS_DEPLOYMENT_TRACKER,
    //                                  loaderHandler, new AppDeploymentTracker(entityHandler));
    //    }
}
