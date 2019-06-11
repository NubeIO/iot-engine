package com.nubeiot.edge.module.installer;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.InstallerConfig;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

public final class ServiceInstallerEntityHandler extends EdgeEntityHandler {

    public ServiceInstallerEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected EventModel deploymentEvent() {
        return EdgeInstallerEventBus.SERVICE_DEPLOYMENT;
    }

    @Override
    public Single<EventMessage> initData() {
        InstallerConfig installerCfg = IConfig.from(
            this.sharedDataFunc.apply(EdgeServiceInstallerVerticle.SHARED_INSTALLER_CFG), InstallerConfig.class);
        super.setupServiceRepository(installerCfg.getRepoConfig());
        return this.startupModules().map(r -> EventMessage.success(EventAction.INIT, r));
    }

}
