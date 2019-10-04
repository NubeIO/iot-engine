package com.nubeiot.edge.module.installer.service;

import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.service.ModuleService;

import lombok.NonNull;

public class EdgeModuleService extends ModuleService implements EdgeInstallerService {

    public EdgeModuleService(@NonNull InstallerVerticle verticle) {
        super(verticle);
    }

    public String servicePath() {
        return "";
    }

    public String paramPath() {
        return "/:service_id";
    }

}
