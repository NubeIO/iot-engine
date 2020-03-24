package com.nubeiot.edge.module.installer.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.BackupByAppService;

import lombok.NonNull;

public final class EdgeBackupByAppService extends BackupByAppService implements EdgeInstallerService {

    protected EdgeBackupByAppService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
