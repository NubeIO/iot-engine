package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.ModuleService;

public final class BiosModuleService extends ModuleService implements BiosInstallerService {

    public BiosModuleService(InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
