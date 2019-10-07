package com.nubeiot.edge.module.installer;

import org.jooq.Configuration;

import io.vertx.core.Vertx;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.model.dto.RequestedServiceData;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;

public final class ServiceInstallerEntityHandler extends InstallerEntityHandler {

    public ServiceInstallerEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected AppConfig transformAppConfig(RepositoryConfig repoConfig, RequestedServiceData serviceData,
                                           ITblModule tblModule, AppConfig appConfig) {
        return appConfig;
    }

}
