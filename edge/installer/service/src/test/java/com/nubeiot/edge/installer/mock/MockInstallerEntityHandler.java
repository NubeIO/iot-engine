package com.nubeiot.edge.installer.mock;

import org.jooq.Configuration;

import io.vertx.core.Vertx;

import com.nubeiot.edge.installer.InstallerEntityHandler;

public class MockInstallerEntityHandler extends InstallerEntityHandler {

    public MockInstallerEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

}
