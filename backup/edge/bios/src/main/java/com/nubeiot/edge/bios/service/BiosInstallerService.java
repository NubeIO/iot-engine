package com.nubeiot.edge.bios.service;

import com.nubeiot.edge.installer.service.InstallerService;

public interface BiosInstallerService extends InstallerService {

    default String api() {
        return "bios.installer." + this.getClass().getSimpleName();
    }

    default String rootPath() {
        return "/modules";
    }

}
