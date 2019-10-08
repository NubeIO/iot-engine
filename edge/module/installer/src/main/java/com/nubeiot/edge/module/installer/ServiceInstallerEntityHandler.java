package com.nubeiot.edge.module.installer;

import org.jooq.Configuration;

import io.vertx.core.Vertx;

import com.nubeiot.edge.installer.InstallerEntityHandler;

public final class ServiceInstallerEntityHandler extends InstallerEntityHandler {

    public ServiceInstallerEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

}
