package com.nubeiot.edge.bios.startupmodules.handler;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

public class PendingModuleWithTwoTransactionsInitData extends MockInitDataEntityHandler {

    protected PendingModuleWithTwoTransactionsInitData(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected Single<Integer> initModules() {
        Single<Integer> insert08 = tblModuleDao.insert(
            new TblModule().setServiceId("pending_module_with_two_transactions")
                           .setServiceName("service8")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.now())
                           .setModifiedAt(DateTimes.now())
                           .setSystemConfig(new JsonObject())
                           .setAppConfig(new JsonObject()));
        Single<Integer> insertTransaction08_1 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending_module_with_two_transactions")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.PATCH)
                                .setModifiedAt(OffsetDateTime.of(2019, 5, 3, 12, 20, 25, 0, ZoneOffset.UTC))
                                .setPrevMetadata(new JsonObject(
                                    "{\"service_id" + "\":\"pending_module_with_two_transactions\"," +
                                    "\"service_name" + "\":\"service5" + "\",\"service_type\":\"JAVA\"," +
                                    "\"version\":\"1.0.0\"," + "\"published_by\":null," + "\"state\":\"DISABLED\"," +
                                    "\"created_at\":\"2019-05-02T09:15:37.230Z\"," +
                                    "\"modified_at\":\"2019-05-02T09:15:37.230Z\",\"deploy_id\":null," +
                                    "\"deploy_config\":{},\"deploy_location\":null}\t ")));

        Single<Integer> insertTransaction08_2 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending_module_with_two_transactions")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.CREATE)
                                .setModifiedAt(OffsetDateTime.of(2019, 5, 3, 12, 20, 30, 0, ZoneOffset.UTC)));
        return Single.zip(insert08, insertTransaction08_1, insertTransaction08_2, (r1, r2, r3) -> r1 + r2 + r3);
    }

}
