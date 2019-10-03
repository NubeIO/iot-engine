package com.nubeiot.edge.module.installer;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.InstallerEntityHandler;
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
    public Single<EventMessage> initData() {
        return this.startupModules().map(r -> EventMessage.success(EventAction.INIT, r));
    }

}
