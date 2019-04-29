package com.nubeiot.edge.bios;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

public class MockEdgeEntityHandler extends EdgeEntityHandler {

    private final TblModuleDao tblModuleDao;

    protected MockEdgeEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
        this.tblModuleDao = new TblModuleDao(jooqConfig, getVertx());
    }

    @Override
    protected EventModel deploymentEvent() {
        return EdgeInstallerEventBus.BIOS_DEPLOYMENT;
    }

    @Override
    public Single<EventMessage> initData() {
        Single<Integer> insert00 = tblModuleDao.insert(new TblModule().setServiceId("1")
                                                                      .setServiceName("service1")
                                                                      .setServiceType(ModuleType.JAVA)
                                                                      .setVersion("1.0.0")
                                                                      .setState(State.ENABLED)
                                                                      .setCreatedAt(DateTimes.nowUTC())
                                                                      .setModifiedAt(DateTimes.nowUTC()));

        Single<Integer> insert01 = tblModuleDao.insert(new TblModule().setServiceId("2")
                                                                      .setServiceName("service2")
                                                                      .setServiceType(ModuleType.JAVA)
                                                                      .setVersion("1.0.0")
                                                                      .setState(State.ENABLED)
                                                                      .setCreatedAt(DateTimes.nowUTC())
                                                                      .setModifiedAt(DateTimes.nowUTC()));
        return Single.zip(insert00, insert01, (r1, r2) -> r1 + r2)
                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    }

}
