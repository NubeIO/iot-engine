package com.nubeiot.edge.module.installer.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.TransactionService;

import lombok.NonNull;

public final class EdgeTransactionService extends TransactionService implements EdgeInstallerService {

    public EdgeTransactionService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
