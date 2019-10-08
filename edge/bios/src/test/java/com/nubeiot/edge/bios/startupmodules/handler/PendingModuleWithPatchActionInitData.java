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
import com.nubeiot.edge.installer.model.tables.pojos.TblModule;
import com.nubeiot.edge.installer.model.tables.pojos.TblTransaction;

public class PendingModuleWithPatchActionInitData extends MockInitDataEntityHandler {

    protected PendingModuleWithPatchActionInitData(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected Single<Integer> initModules() {
        Single<Integer> insert04 = tblModuleDao.insert(
            new TblModule().setServiceId("pending-service-with-transaction-is-wip-prestate-action-is-patch")
                           .setServiceName("service4")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.now())
                           .setModifiedAt(DateTimes.now())
                           .setSystemConfig(new JsonObject())
                           .setAppConfig(new JsonObject()));

        Single<Integer> insertTransaction04 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending-service-with-transaction-is-wip-prestate-action-is-patch")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.PATCH)
                                .setModifiedAt(DateTimes.now())
                                .setPrevMetadata(new JsonObject(
                                    "{\"service_id\":\"pending-service-with-transaction-is-wip" +
                                    "-prestate-action-is-patch\"," +
                                    "\"service_name\":\"service4\",\"service_type\":\"JAVA\"," +
                                    "\"version\":\"1.0.0\",\"published_by\":null," + "\"state\":\"PENDING\"," +
                                    "\"created_at\":\"2019-05-02T09:15:37.230Z\"," +
                                    "\"modified_at\":\"2019-05-02T09:15:37.230Z\"," +
                                    "\"deploy_id\":null,\"deploy_config\":{}," + "\"deploy_location\":null}\t")));
        return Single.zip(insert04, insertTransaction04, Integer::sum);
    }

}
