package com.nubeiot.edge.bios.installer;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.edge.core.EdgeEntityHandler;
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
        return this.startupModules().map(r -> EventMessage.success(EventAction.INIT, r));
    }

}
