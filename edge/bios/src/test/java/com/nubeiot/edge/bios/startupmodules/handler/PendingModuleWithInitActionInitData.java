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
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;

public class PendingModuleWithInitActionInitData extends MockInitDataEntityHandler {

    protected PendingModuleWithInitActionInitData(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected Single<Integer> initModules() {
        Single<Integer> insert01 = tblModuleDao.insert(
            new TblModule().setServiceId("pending-service-with-transaction-is-wip-prestate-action-is-init")
                           .setServiceName("service1")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setSystemConfig(new JsonObject())
                           .setAppConfig(new JsonObject())
                           .setSecretConfig(new JsonObject()));
        Single<Integer> insertTransaction01 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending-service-with-transaction-is-wip-prestate-action-is-init")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.INIT)
                                .setModifiedAt(DateTimes.nowUTC()));
        return Single.zip(insert01, insertTransaction01, (r1, r2) -> r1 + r2);
    }

}
