package com.nubeiot.edge.bios.startupmodules;

import com.nubeiot.edge.bios.EdgeBiosVerticle;
import com.nubeiot.edge.bios.loader.MockModuleLoader;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.service.DeployerDefinition;
import com.nubeiot.edge.core.service.DeployerPostService;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.NonNull;

public class MockBiosStartupModulesVerticle extends EdgeBiosVerticle {

    private final Class<? extends InstallerEntityHandler> entityHandlerClass;

    MockBiosStartupModulesVerticle(Class<? extends InstallerEntityHandler> entityHandlerClass) {
        this.entityHandlerClass = entityHandlerClass;
    }

    @Override
    protected Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return this.entityHandlerClass;
    }

    @Override
    protected @NonNull DeployerDefinition deploymentService() {
        return DeployerDefinition.create(InstallerEventModel.BIOS_DEPLOYMENT, InstallerEventModel.BIOS_POST_DEPLOYMENT,
                                         new MockModuleLoader(null), new DeployerPostService(getEntityHandler()));
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
