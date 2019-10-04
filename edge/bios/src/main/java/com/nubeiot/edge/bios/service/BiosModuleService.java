package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.service.ModuleService;

import lombok.NonNull;

public class BiosModuleService extends ModuleService implements BiosInstallerService {

    public BiosModuleService(@NonNull InstallerVerticle verticle) {
        super(verticle);
    }

    public String servicePath() {
        return "";
    }

    public String paramPath() {
        return "/:service_id";
    }

}
