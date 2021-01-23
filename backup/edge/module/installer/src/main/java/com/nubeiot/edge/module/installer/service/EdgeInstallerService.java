package com.nubeiot.edge.module.installer.service;

import com.nubeiot.edge.installer.service.InstallerService;

public interface EdgeInstallerService extends InstallerService {

    default String api() {
        return "bios.installer.service." + this.getClass().getSimpleName();
    }

    @Override
    default String rootPath() {
        return "/services";
    }

}
