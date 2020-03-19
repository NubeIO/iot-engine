package com.nubeiot.edge.module.installer.service;

import com.nubeiot.edge.installer.InstallerEntityHandler;
import com.nubeiot.edge.installer.service.TransactionByAppService;

import lombok.NonNull;

public final class EdgeTransactionByModuleService extends TransactionByAppService implements EdgeInstallerService {

    public EdgeTransactionByModuleService(@NonNull InstallerEntityHandler entityHandler) {
        super(entityHandler);
    }

}
