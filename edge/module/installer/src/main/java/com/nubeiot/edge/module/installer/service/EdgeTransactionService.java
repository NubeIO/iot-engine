package com.nubeiot.edge.module.installer.service;

import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.service.TransactionService;

import lombok.NonNull;

public class EdgeTransactionService extends TransactionService implements EdgeInstallerService {

    public EdgeTransactionService(@NonNull EdgeVerticle verticle) {
        super(verticle);
    }

    public String servicePath() {
        return "/transactions";
    }

    public String paramPath() {
        return "/:transaction_id";
    }

}
