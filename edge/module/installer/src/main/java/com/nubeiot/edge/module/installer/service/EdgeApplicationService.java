package com.nubeiot.edge.module.installer.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.ApplicationService;

import lombok.NonNull;

public final class EdgeApplicationService extends ApplicationService implements EdgeInstallerService {

    public EdgeApplicationService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
