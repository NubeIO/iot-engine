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

public class PendingModuleWithUpdateActionInitData extends MockInitDataEntityHandler {

    protected PendingModuleWithUpdateActionInitData(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected Single<Integer> initModules() {
        Single<Integer> insert03 = tblModuleDao.insert(
            new TblModule().setServiceId("pending-service-with-transaction-is-wip-prestate-action-is-update")
                           .setServiceName("service3")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));

        Single<Integer> insertTransaction03 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending-service-with-transaction-is-wip-prestate-action-is-update")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.UPDATE)
                                .setModifiedAt(DateTimes.nowUTC())
                                .setPrevState(new JsonObject(
                                    "{\"service_id\":\"pending-service-with-transaction-is-wip" +
                                    "-prestate-action-is-update\"," +
                                    "\"service_name\":\"service3\",\"service_type\":\"JAVA\"," +
                                    "\"version\":\"1.0.0\",\"published_by\":null," + "\"state\":\"PENDING\"," +
                                    "\"created_at\":\"2019-05-02T09:15:37.230\"," +
                                    "\"modified_at\":\"2019-05-02T09:15:37.230\"," +
                                    "\"deploy_id\":null,\"deploy_config\":{}," + "\"deploy_location\":null}\t")));
        return Single.zip(insert03, insertTransaction03, (r1, r2) -> r1 + r2);
    }

}
