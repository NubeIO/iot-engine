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
import com.nubeiot.edge.installer.loader.ModuleType;
import com.nubeiot.edge.installer.model.tables.pojos.Application;
import com.nubeiot.edge.installer.model.tables.pojos.DeployTransaction;

public class PendingModuleWithCreateActionInitData extends MockInitDataEntityHandler {

    protected PendingModuleWithCreateActionInitData(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected Single<Integer> initModules() {
        Single<Integer> insert02 = applicationDao.insert(
            new Application().setAppId("pending-service-with-transaction-is-wip-prestate-action-is-create")
                             .setServiceName("service2")
                             .setServiceType(ModuleType.JAVA)
                             .setVersion("1.0.0")
                             .setState(State.PENDING)
                             .setCreatedAt(DateTimes.now())
                             .setModifiedAt(DateTimes.now())
                             .setSystemConfig(new JsonObject())
                             .setAppConfig(new JsonObject()));
        Single<Integer> insertTransaction02 = tblTransactionDao.insert(
            new DeployTransaction().setTransactionId(UUID.randomUUID().toString())
                                   .setAppId("pending-service-with-transaction-is-wip-prestate-action-is-create")
                                   .setStatus(Status.WIP)
                                   .setEvent(EventAction.CREATE)
                                   .setModifiedAt(DateTimes.now()));
        return Single.zip(insert02, insertTransaction02, Integer::sum);
    }

}
