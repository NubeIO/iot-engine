package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.TransactionService;

import lombok.NonNull;

public final class BiosTransactionService extends TransactionService implements BiosInstallerService {

    public BiosTransactionService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
