package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.service.ModuleService;

public final class BiosModuleService extends ModuleService implements BiosInstallerService {

    public BiosModuleService(InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
