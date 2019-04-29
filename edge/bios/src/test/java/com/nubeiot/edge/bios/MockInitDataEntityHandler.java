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

public class MockInitDataEntityHandler extends EdgeEntityHandler {

    private final TblModuleDao tblModuleDao;

    protected MockInitDataEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
        this.tblModuleDao = new TblModuleDao(jooqConfig, getVertx());
    }

    @Override
    public Single<EventMessage> initData() {
        Single<Integer> insert00 = tblModuleDao.insert(new TblModule().setServiceId("enabled-service")
                                                                      .setServiceName("service0")
                                                                      .setServiceType(ModuleType.JAVA)
                                                                      .setVersion("1.0.0")
                                                                      .setState(State.ENABLED)
                                                                      .setCreatedAt(DateTimes.nowUTC())
                                                                      .setModifiedAt(DateTimes.nowUTC())
                                                                      .setDeployConfig(new JsonObject()));

        Single<Integer> insert01 = tblModuleDao.insert(
            new TblModule().setServiceId("pending-service-with-transaction" + "-is-wip-prestate-action-is-init")
                           .setServiceName("service1")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.ENABLED)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));

        Single<Integer> insert02 = tblModuleDao.insert(
            new TblModule().setServiceId("pending-service-with-transaction" + "-is-wip-prestate-action-is-create")
                           .setServiceName("service2")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));

        Single<Integer> insert03 = tblModuleDao.insert(
            new TblModule().setServiceId("pending-service-with-transaction" + "-is-wip-prestate-action-is-update")
                           .setServiceName("service3")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));

        Single<Integer> insert04 = tblModuleDao.insert(
            new TblModule().setServiceId("pending-service-with-transaction" + "-is-wip-prestate-action-is-patch")
                           .setServiceName("service4")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));

        Single<Integer> insert05 = tblModuleDao.insert(new TblModule().setServiceId(
            "pending-service-with-transaction" + "-is-wip-prestate-action-is-update" + "-disabled")
                                                                      .setServiceName("service5")
                                                                      .setServiceType(ModuleType.JAVA)
                                                                      .setVersion("1.0.0")
                                                                      .setState(State.PENDING)
                                                                      .setCreatedAt(DateTimes.nowUTC())
                                                                      .setModifiedAt(DateTimes.nowUTC())
                                                                      .setDeployConfig(new JsonObject()));

        Single<Integer> insert06 = tblModuleDao.insert(new TblModule().setServiceId(
            "pending-service-with-transaction" + "-is-wip-prestate-action-is-patch" + "-disabled")
                                                                      .setServiceName("service6")
                                                                      .setServiceType(ModuleType.JAVA)
                                                                      .setVersion("1.0.0")
                                                                      .setState(State.PENDING)
                                                                      .setCreatedAt(DateTimes.nowUTC())
                                                                      .setModifiedAt(DateTimes.nowUTC())
                                                                      .setDeployConfig(new JsonObject()));
        return Single.zip(insert00, insert01, insert03, (r1, r2, r3) -> r1 + r2 + r3)
                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    }

    @Override
    protected EventModel deploymentEvent() {
        return null;
    }

}
