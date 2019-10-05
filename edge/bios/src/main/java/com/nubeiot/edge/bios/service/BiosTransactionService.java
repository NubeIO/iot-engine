package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.service.TransactionService;

import lombok.NonNull;

public final class BiosTransactionService extends TransactionService implements BiosInstallerService {

    public BiosTransactionService(@NonNull InstallerVerticle verticle) {
        super(verticle);
    }

}
