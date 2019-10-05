package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.service.TransactionService;

import lombok.NonNull;

public final class BiosTransactionService extends TransactionService implements BiosInstallerService {

    public BiosTransactionService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
