package com.nubeiot.edge.bios;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jooq.Configuration;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.daos.TblTransactionDao;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.edge.core.model.tables.pojos.TblTransaction;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

public class MockInitDataEntityHandler extends EdgeEntityHandler {

    private final TblModuleDao tblModuleDao;
    private final TblTransactionDao tblTransactionDao;

    protected MockInitDataEntityHandler(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
        this.tblModuleDao = new TblModuleDao(jooqConfig, getVertx());
        this.tblTransactionDao = new TblTransactionDao(jooqConfig, getVertx());
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
            new TblModule().setServiceId("pending-service-with-transaction-is-wip-prestate-action-is-init")
                           .setServiceName("service1")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));
        Single<Integer> insertTransaction01 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending-service-with-transaction-is-wip-prestate-action-is-init")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.INIT)
                                .setModifiedAt(DateTimes.nowUTC()));

        Single<Integer> insert02 = tblModuleDao.insert(
            new TblModule().setServiceId("pending-service-with-transaction-is-wip-prestate-action-is-create")
                           .setServiceName("service2")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));
        Single<Integer> insertTransaction02 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending-service-with-transaction-is-wip-prestate-action-is-create")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.CREATE)
                                .setModifiedAt(DateTimes.nowUTC()));

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

        Single<Integer> insert04 = tblModuleDao.insert(
            new TblModule().setServiceId("pending-service-with-transaction-is-wip-prestate-action-is-patch")
                           .setServiceName("service4")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));

        Single<Integer> insertTransaction04 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending-service-with-transaction-is-wip-prestate-action-is-patch")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.PATCH)
                                .setModifiedAt(DateTimes.nowUTC())
                                .setPrevState(new JsonObject(
                                    "{\"service_id\":\"pending-service-with-transaction-is-wip" +
                                    "-prestate-action-is-patch\"," +
                                    "\"service_name\":\"service4\",\"service_type\":\"JAVA\"," +
                                    "\"version\":\"1.0.0\",\"published_by\":null," + "\"state\":\"PENDING\"," +
                                    "\"created_at\":\"2019-05-02T09:15:37.230\"," +
                                    "\"modified_at\":\"2019-05-02T09:15:37.230\"," +
                                    "\"deploy_id\":null,\"deploy_config\":{}," + "\"deploy_location\":null}\t")));

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

        Single<Integer> insert08 = tblModuleDao.insert(
            new TblModule().setServiceId("pending_module_with_two_transactions")
                           .setServiceName("service8")
                           .setServiceType(ModuleType.JAVA)
                           .setVersion("1.0.0")
                           .setState(State.PENDING)
                           .setCreatedAt(DateTimes.nowUTC())
                           .setModifiedAt(DateTimes.nowUTC())
                           .setDeployConfig(new JsonObject()));
        Single<Integer> insertTransaction08_1 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending_module_with_two_transactions")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.PATCH)
                                .setPrevState(new JsonObject())
                                .setModifiedAt(LocalDateTime.of(2019, 5, 3, 12, 20, 25))
                                .setPrevState(new JsonObject(
                                    "{\"service_id" + "\":\"pending_module_with_two_transactions\"," +
                                    "\"service_name" + "\":\"service5" + "\",\"service_type\":\"JAVA\"," +
                                    "\"version\":\"1.0.0\"," + "\"published_by\":null," + "\"state\":\"DISABLED\"," +
                                    "\"created_at\":\"2019-05-02T09:15:37" +
                                    ".230\",\"modified_at\":\"2019-05-02T09:15" + ":37.230\",\"deploy_id\":null," +
                                    "\"deploy_config\":{}," + "\"deploy_location\":null}\t ")));

        Single<Integer> insertTransaction08_2 = tblTransactionDao.insert(
            new TblTransaction().setTransactionId(UUID.randomUUID().toString())
                                .setModuleId("pending_module_with_two_transactions")
                                .setStatus(Status.WIP)
                                .setEvent(EventAction.CREATE)
                                .setPrevState(new JsonObject())
                                .setModifiedAt(LocalDateTime.of(2019, 5, 3, 12, 20, 30))
                                .setPrevState(new JsonObject()));

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

        final Single<Integer> insertModules = Single.zip(insert00, insert01, insert02, insert03, insert04, insert05,
                                                         insert06, insert07,
                                                         (r1, r2, r3, r4, r5, r6, r7, r8) -> r1 + r2 + r3 + r4 + r5 +
                                                                                             r6 + r7 + r8);

        final Single<Integer> insertTransactions = Single.zip(insertTransaction01, insertTransaction02,
                                                              insertTransaction03, insertTransaction04,
                                                              insertTransaction05, insertTransaction06,
                                                              (r1, r2, r3, r4, r5, r6) -> r1 + r2 + r3 + r4 + r5 + r6);
        final Single<Integer> insertMore = Single.zip(insert08, insert09, insert10, insertTransaction08_1,
                                                      insertTransaction08_2, insertTransaction09_1,
                                                      insertTransaction09_2, insertTransaction10,
                                                      (r1, r2, r3, r4, r5, r6, r7, r8) -> r1 + r2 + r3 + r4 + r5 + r6 +
                                                                                          r7 + r8);
        final Single<Integer> records = Single.zip(insertModules, insertTransactions, insertMore,
                                                   (r1, r2, r3) -> r1 + r2 + r3);
        final Single<JsonObject> jsonObjectSingle = this.startupModules();
        return Single.zip(records, jsonObjectSingle, (r1, r2) -> r1.intValue() + r2.toString())
                     .map(r -> EventMessage.success(EventAction.INIT, new JsonObject().put("records", r)));
    }

    @Override
    protected EventModel deploymentEvent() {
        return EdgeInstallerEventBus.BIOS_DEPLOYMENT;
    }

}
