package com.nubeiot.edge.bios.startupmodules.handler;

import java.time.LocalDateTime;
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

public class InvalidModulesInitData extends MockInitDataEntityHandler {

    protected InvalidModulesInitData(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected Single<Integer> initModules() {
        Single<Integer> insert05 = tblModuleDao.insert(
            new TblModule().setServiceId("pending-service-with-transaction-is-wip-prestate-action-is-update-disabled")
                           .setServiceName("service5")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));
        Single<Integer> insertTransaction05 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId(
                                    "pending-service-with-transaction-is-wip-prestate-action-is-update-disabled")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.UPDATE)
                                .setPrevState(new JsonObject())
                                .setModifiedAt(DateTimes.nowUTC())
                                .setPrevState(new JsonObject(
                                    "{\"service_id" + "\": \"pending" + "-service-with" + "-transaction" + "-is-wip" +
                                    "-prestate" + "-action-is" + "-patch" + "-disabled\"," + "\"service_name" +
                                    "\": \"service6" +
                                    "\",\"service_type\": \"JAVA\",\"version\": \"1.0.0\",\"published_by\": null," +
                                    "\"state\": \"DISABLED\",\"created_at\": \"2019-05-02T09:15:37.230\"," +
                                    "\"modified_at\": " +
                                    "\"2019-05-02T09:15:37.230\",\"deploy_id\": null,\"deploy_config\": {}," +
                                    "\"deploy_location\": null }")));

        Single<Integer> insert06 = tblModuleDao.insert(new TblModule().setServiceId(
            "pending-service-with-transaction" + "-is-wip-prestate-action-is-patch-disabled")
                                                                      .setServiceName("service6")
                                                                      .setServiceType(ModuleType.JAVA)
                                                                      .setVersion("1.0.0")
                                                                      .setState(State.PENDING)
                                                                      .setCreatedAt(DateTimes.nowUTC())
                                                                      .setModifiedAt(DateTimes.nowUTC())
                                                                      .setDeployConfig(new JsonObject()));
        Single<Integer> insertTransaction06 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId(
                                    "pending-service-with-transaction-is-wip-prestate-action-is-patch-disabled")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.PATCH)
                                .setPrevState(new JsonObject())
                                .setModifiedAt(DateTimes.nowUTC())
                                .setPrevState(new JsonObject(
                                    "{\"service_id" + "\":\"pending" + "-service-with" + "-transaction" + "-is-wip" +
                                    "-prestate" + "-action-is" + "-update" + "-disabled\"," + "\"service_name" +
                                    "\":\"service5" + "\",\"service_type\":\"JAVA\"," + "\"version\":\"1.0.0\"," +
                                    "\"published_by\":null," + "\"state\":\"DISABLED\"," +
                                    "\"created_at\":\"2019-05-02T09:15:37" +
                                    ".230\",\"modified_at\":\"2019-05-02T09:15" + ":37.230\",\"deploy_id\":null," +
                                    "\"deploy_config\":{}," + "\"deploy_location\":null}\t ")));

        Single<Integer> insert07 = tblModuleDao.insert(new TblModule().setServiceId("disabled-module")
                                                                      .setServiceName("service7")
                                                                      .setServiceType(ModuleType.JAVA)
                                                                      .setVersion("1.0.0")
                                                                      .setState(State.DISABLED)
                                                                      .setCreatedAt(DateTimes.nowUTC())
                                                                      .setModifiedAt(DateTimes.nowUTC())
                                                                      .setDeployConfig(new JsonObject()));

        Single<Integer> insert09 = tblModuleDao.insert(
            new TblModule().setServiceId("pending_module_with_two_transactions_invalid")
                           .setServiceName("service9")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));
        Single<Integer> insertTransaction09_1 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending_module_with_two_transactions_invalid")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.CREATE)
                                .setPrevState(new JsonObject())
                                .setModifiedAt(LocalDateTime.of(2019, 5, 3, 12, 20, 25))
                                .setPrevState(new JsonObject()));

        Single<Integer> insertTransaction09_2 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending_module_with_two_transactions_invalid")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.PATCH)
                                .setPrevState(new JsonObject())
                                .setModifiedAt(LocalDateTime.of(2019, 5, 3, 12, 20, 30))
                                .setPrevState(new JsonObject(
                                    "{\"service_id" + "\":\"pending_module_with_two_transactions_invalid\"," +
                                    "\"service_name" + "\":\"service9" + "\",\"service_type\":\"JAVA\"," +
                                    "\"version\":\"1.0.0\"," + "\"published_by\":null," + "\"state\":\"DISABLED\"," +
                                    "\"created_at\":\"2019-05-02T09:15:37" +
                                    ".230\",\"modified_at\":\"2019-05-02T09:15" + ":37.230\",\"deploy_id\":null," +
                                    "\"deploy_config\":{}," + "\"deploy_location\":null}\t ")));

        Single<Integer> insert10 = tblModuleDao.insert(new TblModule().setServiceId("pending-but-failed-module")
                                                                      .setServiceName("service10")
                                                                      .setServiceType(ModuleType.JAVA)
                                                                      .setVersion("1.0.0")
                                                                      .setState(State.PENDING)
                                                                      .setCreatedAt(DateTimes.nowUTC())
                                                                      .setModifiedAt(DateTimes.nowUTC())
                                                                      .setDeployConfig(new JsonObject()));
        Single<Integer> insertTransaction10 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending-but-failed-module")
                                .setStatus(Status.FAILED)
                                .setEvent(EventAction.CREATE)
                                .setPrevState(new JsonObject())
                                .setModifiedAt(LocalDateTime.of(2019, 5, 3, 12, 20, 30))
                                .setPrevState(new JsonObject()));

        final Single<Integer> insertModules = Single.zip(insert05, insert06, insert07, insert09, insert10,
                                                         (r1, r2, r3, r4, r5) -> r1 + r2 + r3 + r4 + r5);

        final Single<Integer> insertTransactions = Single.zip(insertTransaction05, insertTransaction06,
                                                              insertTransaction09_1, insertTransaction09_2,
                                                              insertTransaction10,
                                                              (r1, r2, r3, r4, r5) -> r1 + r2 + r3 + r4 + r5);
        return Single.zip(insertModules, insertTransactions, (r1, r2) -> r1 + r2);
    }

}
