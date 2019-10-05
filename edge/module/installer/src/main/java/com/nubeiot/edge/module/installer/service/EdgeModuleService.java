package com.nubeiot.edge.module.installer.service;

import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.service.ModuleService;

import lombok.NonNull;

public final class EdgeModuleService extends ModuleService implements EdgeInstallerService {

    public EdgeModuleService(@NonNull InstallerVerticle verticle) {
        super(verticle);
    }

}
