package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.ApplicationService;

public final class BiosApplicationService extends ApplicationService implements BiosInstallerService {

    public BiosApplicationService(InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
