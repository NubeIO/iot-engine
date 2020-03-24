package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.TransactionByAppService;

import lombok.NonNull;

public final class BiosTransactionByAppService extends TransactionByAppService implements BiosInstallerService {

    public BiosTransactionByAppService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
