package com.nubeiot.edge.module.installer.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.ModuleService;

import lombok.NonNull;

public final class EdgeModuleService extends ModuleService implements EdgeInstallerService {

    public EdgeModuleService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
