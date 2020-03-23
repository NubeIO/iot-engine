package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.BackupByAppService;

import lombok.NonNull;

public final class BiosBackupByAppService extends BackupByAppService implements BiosInstallerService {

    protected BiosBackupByAppService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
