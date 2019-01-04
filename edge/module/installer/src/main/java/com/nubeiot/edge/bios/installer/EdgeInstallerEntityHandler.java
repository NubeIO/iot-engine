package com.nubeiot.edge.bios.installer;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.edge.core.EdgeEntityHandler;

public class EdgeInstallerEntityHandler extends EdgeEntityHandler {

    public EdgeInstallerEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    public Single<EventMessage> initData() {
        return this.startupModules().map(r -> EventMessage.success(EventAction.INIT, r));
    }

}
