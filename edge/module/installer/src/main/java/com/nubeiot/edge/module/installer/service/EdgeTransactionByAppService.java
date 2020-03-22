package com.nubeiot.edge.module.installer.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.TransactionByAppService;

import lombok.NonNull;

public final class EdgeTransactionByAppService extends TransactionByAppService implements EdgeInstallerService {

    public EdgeTransactionByAppService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
