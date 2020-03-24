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
import com.nubeiot.edge.installer.loader.VertxModuleType;
import com.nubeiot.edge.installer.model.tables.pojos.Application;
import com.nubeiot.edge.installer.model.tables.pojos.DeployTransaction;

public class InvalidModulesInitData extends MockInitDataEntityHandler {

    protected InvalidModulesInitData(Configuration configuration, Vertx vertx) {
        super(configuration, vertx);
    }

    @Override
    protected Single<Integer> initModules() {
        final Application service5 = new Application().setAppId(
            "pending-service-with-transaction-is-wip-prestate-action-is-update-disabled")
                                                      .setServiceName("service5")
                                                      .setServiceType(VertxModuleType.JAVA)
                                                      .setVersion("1.0.0")
                                                      .setState(State.PENDING)
                                                      .setCreatedAt(DateTimes.now())
                                                      .setModifiedAt(DateTimes.now())
                                                      .setSystemConfig(new JsonObject())
                                                      .setAppConfig(new JsonObject());
        Single<Integer> insert05 = applicationDao.insert(service5);
        Single<Integer> insertTransaction05 = tblTransactionDao.insert(
            new DeployTransaction().setTransactionId(UUID.randomUUID().toString())
                                   .setAppId(service5.getAppId())
                                   .setStatus(Status.WIP)
                                   .setEvent(EventAction.UPDATE)
                                   .setModifiedAt(DateTimes.now())
                                   .setPrevMetadata(new JsonObject(
                                       "{\"service_id\": \"pending-service-with-transaction-is-wip-prestate-action-is" +
                                       "-patch-disabled\",\"service_name\": \"service6\",\"service_type\": \"JAVA\"," +
                                       "\"version\": \"1.0.0\",\"published_by\": null,\"state\": \"DISABLED\"," +
                                       "\"created_at\": \"2019-05-02T09:15:37.230Z\"," +
                                       "\"modified_at\": \"2019-05-02T09:15:37.230Z\",\"deploy_id\": null," +
                                       "\"deploy_config\": {},\"deploy_location\": null }")));

        final Application service6 = new Application().setAppId(
            "pending-service-with-transaction-is-wip-prestate-action-is-patch-disabled")
                                                      .setServiceName("service6")
                                                      .setServiceType(VertxModuleType.JAVA)
                                                      .setVersion("1.0.0")
                                                      .setState(State.PENDING)
                                                      .setCreatedAt(DateTimes.now())
                                                      .setModifiedAt(DateTimes.now())
                                                      .setSystemConfig(new JsonObject())
                                                      .setAppConfig(new JsonObject());
        Single<Integer> insert06 = applicationDao.insert(service6);
        Single<Integer> insertTransaction06 = tblTransactionDao.insert(
            new DeployTransaction().setTransactionId(UUID.randomUUID().toString())
                                   .setAppId(service6.getAppId())
                                   .setStatus(Status.WIP)
                                   .setEvent(EventAction.PATCH)
                                   .setModifiedAt(DateTimes.now())
                                   .setPrevMetadata(new JsonObject(
                                       "{\"service_id\":\"pending-service-with-transaction-is-wip-prestate-action-is" +
                                       "-update-disabled\",\"service_name" +
                                       "\":\"service5\",\"service_type\":\"JAVA\"," + "\"version\":\"1.0.0\"," +
                                       "\"published_by\":null," + "\"state\":\"DISABLED\"," +
                                       "\"created_at\":\"2019-05-02T09:15:37.230Z\"," +
                                       "\"modified_at\":\"2019-05-02T09:15:37.230Z\",\"deploy_id\":null," +
                                       "\"deploy_config\":{}," + "\"deploy_location\":null}\t ")));

        Single<Integer> insert07 = applicationDao.insert(new Application().setAppId("disabled-module")
                                                                          .setServiceName("service7")
                                                                          .setServiceType(VertxModuleType.JAVA)
                                                                          .setVersion("1.0.0")
                                                                          .setState(State.DISABLED)
                                                                          .setCreatedAt(DateTimes.now())
                                                                          .setModifiedAt(DateTimes.now())
                                                                          .setAppConfig(new JsonObject())
                                                                          .setSystemConfig(new JsonObject()));

        Single<Integer> insert09 = applicationDao.insert(
            new Application().setAppId("pending_module_with_two_transactions_invalid")
                             .setServiceName("service9")
                             .setServiceType(VertxModuleType.JAVA)
                             .setVersion("1.0.0")
                             .setState(State.PENDING)
                             .setCreatedAt(DateTimes.now())
                             .setModifiedAt(DateTimes.now())
                             .setSystemConfig(new JsonObject())
                             .setAppConfig(new JsonObject()));
        Single<Integer> insertTransaction09_1 = tblTransactionDao.insert(
            new DeployTransaction().setTransactionId(UUID.randomUUID().toString())
                                   .setAppId("pending_module_with_two_transactions_invalid")
                                   .setStatus(Status.WIP)
                                   .setEvent(EventAction.CREATE)
                                   .setModifiedAt(OffsetDateTime.of(2019, 5, 3, 12, 20, 25, 0, ZoneOffset.UTC)));

        Single<Integer> insertTransaction09_2 = tblTransactionDao.insert(
            new DeployTransaction().setTransactionId(UUID.randomUUID().toString())
                                   .setAppId("pending_module_with_two_transactions_invalid")
                                   .setStatus(Status.WIP)
                                   .setEvent(EventAction.PATCH)
                                   .setModifiedAt(OffsetDateTime.of(2019, 5, 3, 12, 20, 30, 0, ZoneOffset.UTC))
                                   .setPrevMetadata(new JsonObject(
                                       "{\"service_id\":\"pending_module_with_two_transactions_invalid\"," +
                                       "\"service_name\":\"service9\",\"service_type\":\"JAVA\"," +
                                       "\"version\":\"1.0.0\",\"published_by\":null,\"state\":\"DISABLED\"," +
                                       "\"created_at\":\"2019-05-02T09:15:37.230Z\"," +
                                       "\"modified_at\":\"2019-05-02T09:15:37.230Z\",\"deploy_id\":null," +
                                       "\"deploy_config\":{}," + "\"deploy_location\":null}")));

        Single<Integer> insert10 = applicationDao.insert(new Application().setAppId("pending-but-failed-module")
                                                                          .setServiceName("service10")
                                                                          .setServiceType(VertxModuleType.JAVA)
                                                                          .setVersion("1.0.0")
                                                                          .setState(State.PENDING)
                                                                          .setCreatedAt(DateTimes.now())
                                                                          .setModifiedAt(DateTimes.now()));

        Single<Integer> insertTransaction10 = tblTransactionDao.insert(
            new DeployTransaction().setTransactionId(UUID.randomUUID().toString())
                                   .setAppId("pending-but-failed-module")
                                   .setStatus(Status.FAILED)
                                   .setEvent(EventAction.CREATE)
                                   .setModifiedAt(OffsetDateTime.of(2019, 5, 3, 12, 20, 30, 0, ZoneOffset.UTC)));

        final Single<Integer> insertModules = Single.zip(insert05, insert06, insert07, insert09, insert10,
                                                         (r1, r2, r3, r4, r5) -> r1 + r2 + r3 + r4 + r5);

        final Single<Integer> insertTransactions = Single.zip(insertTransaction05, insertTransaction06,
                                                              insertTransaction09_1, insertTransaction09_2,
                                                              insertTransaction10,
                                                              (r1, r2, r3, r4, r5) -> r1 + r2 + r3 + r4 + r5);
        return Single.zip(insertModules, insertTransactions, Integer::sum);
    }

}
