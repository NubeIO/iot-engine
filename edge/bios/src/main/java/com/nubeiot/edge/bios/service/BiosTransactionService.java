package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.service.TransactionService;

import lombok.NonNull;

public class BiosTransactionService extends TransactionService implements BiosInstallerService {

    public BiosTransactionService(@NonNull EdgeVerticle verticle) {
        super(verticle);
    }

    public String servicePath() {
        return "/transactions";
    }

    public String paramPath() {
        return "/:transaction_id";
    }

}
