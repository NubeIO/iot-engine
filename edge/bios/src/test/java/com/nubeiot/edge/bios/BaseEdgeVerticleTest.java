package com.nubeiot.edge.bios;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.jooq.SQLDialect;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.NubeConfig.DeployConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.sql.SqlConfig;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;

public abstract class BaseEdgeVerticleTest {

    static final String GROUP_ID = "com.nubeiot.edge.module";
    static final String ARTIFACT_ID = "mytest";
    static final String VERSION = "1.0.0";
    static final String SERVICE_NAME = "bios-mytest";
    static final String MODULE_ID = GROUP_ID + ":" + ARTIFACT_ID;
    static final String APP_CONFIG = "{\"__sql__\":{\"dialect\":\"H2\",\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:file:" +
                                     "./bios-installer\",\"minimumIdle\":1,\"maximumPoolSize\":2," +
                                     "\"connectionTimeout\":30000,\"idleTimeout\":180000,\"maxLifetime\":300000}}}";
    static final JsonObject DEPLOY_CONFIG = new JsonObject(
        "{\"__deploy__\":{\"ha\":false,\"instances\":1," + "\"maxWorkerExecuteTime\":60000000000," +
        "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\"," + "\"multiThreaded\":false,\"worker\":false," +
        "\"workerPoolSize\":20},\"dataDir\":\"file:///root/" + ".nubeio/com.nubeiot.edge.module_installer\"," +
        "\"__app__\": " + APP_CONFIG + " }");
    private static boolean isAvailable;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    protected Vertx vertx;
    protected EdgeVerticle edgeVerticle;

    protected static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.ERROR);
        if (!isAvailable) {
            StateMachine.init();
            isAvailable = true;
        }
    }

    protected void before(TestContext context) {
        SqlConfig sqlConfig = new SqlConfig();
        sqlConfig.getHikariConfig().setJdbcUrl(getJdbcUrl());
        sqlConfig.setDialect(SQLDialect.H2);

        NubeConfig nubeConfig = new NubeConfig();
        nubeConfig.setDeployConfig(new DeployConfig());

        AppConfig appConfig = new AppConfig();
        appConfig.put("__sql__", sqlConfig.toJson());

        nubeConfig.setAppConfig(appConfig);
        DeploymentOptions options = new DeploymentOptions().setConfig(nubeConfig.toJson());
        this.vertx = Vertx.vertx();
        this.edgeVerticle = initMockupVerticle(context);
        Async async = context.async();
        this.vertx.getDelegate()
                  .deployVerticle(this.edgeVerticle, options,
                                  context.asyncAssertSuccess(result -> TestHelper.testComplete(async)));
        async.awaitSuccess();
    }

    protected void insertModule(TestContext context, TblModule module) {
        Async async = context.async(1);
        this.edgeVerticle.getEntityHandler().getModuleDao().insert(module).subscribe(result -> {
            System.out.println("Insert module successfully!");
            TestHelper.testComplete(async);
        }, error -> {
            context.fail(error);
            TestHelper.testComplete(async);
        });

        async.awaitSuccess();
    }

    public void after(TestContext context) {
        this.vertx.close(context.asyncAssertSuccess());
    }

    protected @NonNull String getJdbcUrl() {
        return "jdbc:h2:file:" + folder.getRoot().toPath().resolve("dbh2local").toString();
    }

    protected AssertmentConsumer getConsumer(TestContext context) {
        return (preDeploymentResult) -> {
            JsonObject serviceBody = new JsonObject().put("service_id", preDeploymentResult.getServiceId());
            EventMessage serviceMessage = EventMessage.success(EventAction.GET_ONE,
                                                               RequestData.builder().body(serviceBody).build());

            JsonObject transactionBody = new JsonObject().put("transaction_id", preDeploymentResult.getTransactionId());
            EventMessage transactionMessage = EventMessage.success(EventAction.GET_ONE,
                                                                   RequestData.builder().body(transactionBody).build());
            final Async async = context.async(2);

            this.vertx.getDelegate()
                      .eventBus()
                      .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), serviceMessage.toJson(), result -> {
                          System.out.println("Asserting module");
                          JsonObject body = (JsonObject) result.result().body();
                          context.assertEquals(body.getString("status"), Status.SUCCESS.name());
                          JsonObject data = body.getJsonObject("data");
                          context.assertEquals(data.getString("state"), State.PENDING.name());
                          TestHelper.testComplete(async);
                      });

            this.vertx.getDelegate()
                      .eventBus()
                      .send(EdgeInstallerEventBus.BIOS_TRANSACTION.getAddress(), transactionMessage.toJson(),
                            result -> {
                                System.out.println("Asserting transaction");
                                JsonObject body = (JsonObject) result.result().body();
                                context.assertEquals(body.getString("status"), Status.SUCCESS.name());
                                JsonObject data = body.getJsonObject("data");
                                context.assertEquals(data.getString("status"), Status.WIP.name());
                                TestHelper.testComplete(async);
                            });
        };
    }

    protected void testingDBUpdated(TestContext context, State expectedModuleState, Status expectedTransactionStatus,
                                    JsonObject expectedConfig) {
        testingDBUpdated(context, expectedModuleState, expectedTransactionStatus, expectedConfig.toString());
    }

    protected void testingDBUpdated(TestContext context, State expectedModuleState, Status expectedTransactionStatus,
                                    String expectedConfig) {
        Async async = context.async(2);
        //Event module is deployed/updated successfully, we still have a gap for DB update.
        long timer = this.vertx.setPeriodic(1000, event -> {
            edgeVerticle.getEntityHandler()
                        .getModuleDao()
                        .findOneById(BaseEdgeVerticleTest.MODULE_ID)
                        .subscribe(result -> {
                            TblModule tblModule = result.orElse(null);
                            context.assertNotNull(tblModule);
                            if (tblModule.getState() != State.PENDING) {
                                System.out.println("Ready. Testing module");
                                context.assertEquals(tblModule.getState(), expectedModuleState);
                                JsonObject actualConfig = IConfig.from(tblModule.getDeployConfig(), NubeConfig.class)
                                                                 .getAppConfig()
                                                                 .toJson();
                                context.assertEquals(actualConfig.toString(), expectedConfig);
                                TestHelper.testComplete(async);
                            }
                        }, error -> {
                            context.fail(error);
                            TestHelper.testComplete(async);
                        });
            edgeVerticle.getEntityHandler()
                        .getTransDao()
                        .findManyByModuleId(Collections.singletonList(BaseEdgeVerticleTest.MODULE_ID))
                        .subscribe(result -> {
                            context.assertNotNull(result);
                            context.assertFalse(result.isEmpty());
                            context.assertEquals(result.size(), 1);
                            if (result.get(0).getStatus() != Status.WIP) {
                                System.out.println("Ready. Testing transaction");
                                context.assertEquals(result.get(0).getStatus(), expectedTransactionStatus);
                                TestHelper.testComplete(async);
                            }
                        }, error -> {
                            context.fail(error);
                            TestHelper.testComplete(async);
                        });
        });

        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async);
        });
    }

    protected abstract EdgeVerticle initMockupVerticle(TestContext context);

    protected void executeThenAssert(EventAction eventAction, TestContext context, JsonObject body,
                                     BiConsumer<JsonObject, Async> handler) {
        EventMessage eventMessage = EventMessage.success(eventAction, RequestData.builder().body(body).build());
        Async async = context.async();
        this.vertx.getDelegate()
                  .eventBus()
                  .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                        context.asyncAssertSuccess(handle -> handler.accept((JsonObject) handle.body(), async)));
    }

    protected void assertModuleState(TestContext context, Async async1, TblModuleDao moduleDao, State expectedState,
                                   String moduleId) {
        moduleDao.findOneById(moduleId).subscribe(result -> {
            TblModule tblModule = result.orElse(null);
            context.assertNotNull(tblModule);
            if (tblModule.getState() != State.PENDING) {
                System.out.println("Checking state of " + moduleId);
                if (Objects.nonNull(expectedState)) {
                    context.assertEquals(tblModule.getState(), expectedState);
                }
                TestHelper.testComplete(async1);
            }
        }, error -> {
            context.fail(error);
            TestHelper.testComplete(async1);
        });
    }

}
