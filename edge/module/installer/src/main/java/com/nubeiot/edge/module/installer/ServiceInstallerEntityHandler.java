package com.nubeiot.edge.module.installer;

import org.jooq.Configuration;

import io.vertx.core.Vertx;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.InstallerConfig.RepositoryConfig;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.RequestedServiceData;
import com.nubeiot.edge.core.model.tables.interfaces.ITblModule;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

public final class ServiceInstallerEntityHandler extends InstallerEntityHandler {

    public ServiceInstallerEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected EventModel deploymentEvent() {
        return InstallerEventModel.SERVICE_DEPLOYMENT;
    }

    @Override
    protected AppConfig transformAppConfig(RepositoryConfig repoConfig, RequestedServiceData serviceData,
                                           ITblModule tblModule, AppConfig appConfig) {
        return appConfig;
    }

}
