package com.nubeiot.edge.bios.startupmodules.handler;

import java.util.UUID;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.installer.loader.VertxModuleType;
import com.nubeiot.edge.installer.model.tables.pojos.Application;
import com.nubeiot.edge.installer.model.tables.pojos.DeployTransaction;

public class PendingModuleWithInitActionInitData extends MockInitDataEntityHandler {

    protected PendingModuleWithInitActionInitData(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected Single<Integer> initModules() {
        Single<Integer> insert01 = applicationDao.insert(
            new Application().setAppId("pending-service-with-transaction-is-wip-prestate-action-is-init")
                             .setServiceName("service1")
                             .setServiceType(VertxModuleType.JAVA)
                             .setVersion("1.0.0")
                             .setState(State.PENDING)
                             .setCreatedAt(DateTimes.now())
                             .setModifiedAt(DateTimes.now())
                             .setSystemConfig(new JsonObject())
                             .setAppConfig(new JsonObject()));
        Single<Integer> insertTransaction01 = tblTransactionDao.insert(
            new DeployTransaction().setTransactionId(UUID.randomUUID().toString())
                                   .setAppId("pending-service-with-transaction-is-wip-prestate-action-is-init")
                                   .setStatus(Status.WIP)
                                   .setEvent(EventAction.INIT)
                                   .setModifiedAt(DateTimes.now()));
        return Single.zip(insert01, insertTransaction01, Integer::sum);
    }

}
