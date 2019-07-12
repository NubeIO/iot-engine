package com.nubeiot.edge.bios;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.jooq.SQLDialect;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

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

import lombok.NonNull;

public abstract class BaseEdgeVerticleTest {

    static final String GROUP_ID = "com.nubeiot.edge.module";
    static final String ARTIFACT_ID = "mytest";
    static final String VERSION = "1.0.0";
    static final String SERVICE_NAME = "bios-mytest";
    static final String MODULE_ID = GROUP_ID + ":" + ARTIFACT_ID;
    static final JsonObject APP_CONFIG = new JsonObject(
        "{\"__kafka__\":{\"__client__\":{\"bootstrap" + ".servers\":[\"localhost:9092\"]}}," +
        "\"__sql__\":{\"dialect\":\"H2\",\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:file:" +
        "./bios-installer\",\"minimumIdle\":1,\"maximumPoolSize\":2," +
        "\"connectionTimeout\":30000,\"idleTimeout\":180000,\"maxLifetime\":300000}}}");
    static final JsonObject APP_SYSTEM_CONFIG = new JsonObject(
        "{\"__deploy__\":{\"ha\":false,\"instances\":1,\"maxWorkerExecuteTime\":60000000000," +
        "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\",\"multiThreaded\":false,\"worker\":false," +
        "\"workerPoolSize\":20},\"dataDir\":\"file:///root/.nubeio/com.nubeiot.edge.module_installer\"}");
    private static boolean isAvailable;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    protected Vertx vertx;
    protected EdgeVerticle edgeVerticle;

    protected static void beforeSuite() {
        TestHelper.setup();
        if (!isAvailable) {
            StateMachine.init();
            isAvailable = true;
        }
    }

    protected void before(TestContext context) {
        DeploymentOptions options = new DeploymentOptions().setConfig(getNubeConfig().toJson());
        Async async = context.async();
        this.vertx = Vertx.vertx();
        this.edgeVerticle = initMockupVerticle(context);
        this.vertx.deployVerticle(this.edgeVerticle, options,
                                  context.asyncAssertSuccess(result -> TestHelper.testComplete(async)));
        async.awaitSuccess();
    }

    protected NubeConfig getNubeConfig() {
        SqlConfig sqlConfig = new SqlConfig();
        sqlConfig.getHikariConfig().setJdbcUrl(getJdbcUrl());
        sqlConfig.setDialect(SQLDialect.H2);

        NubeConfig nubeConfig = new NubeConfig();
        nubeConfig.setDeployConfig(new DeployConfig());

        AppConfig appConfig = new AppConfig();
        appConfig.put("__sql__", sqlConfig.toJson());

        nubeConfig.setAppConfig(appConfig);
        return nubeConfig;
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

    protected void after(TestContext context) {
        this.vertx.close(context.asyncAssertSuccess());
    }

    protected @NonNull String getJdbcUrl() {
        return "jdbc:h2:file:" + folder.getRoot().toPath().resolve("dbh2local").toString();
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
            assertModule(context, expectedModuleState, expectedConfig, async);
            assertTransaction(context, expectedTransactionStatus, async);
        });

        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async);
        });
    }

    private void assertTransaction(TestContext context, Status expectedTransactionStatus, Async async) {
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
    }

    private void assertModule(TestContext context, State expectedModuleState, String expectedConfig, Async async) {
        edgeVerticle.getEntityHandler().getModuleDao().findOneById(BaseEdgeVerticleTest.MODULE_ID).subscribe(result -> {
            TblModule tblModule = result.orElse(null);
            context.assertNotNull(tblModule);
            if (tblModule.getState() != State.PENDING) {
                System.out.println("Ready. Testing module");
                context.assertEquals(tblModule.getState(), expectedModuleState);
                JsonObject actualConfig = IConfig.from(tblModule.getAppConfig(), AppConfig.class).toJson();
                context.assertEquals(actualConfig.toString(), expectedConfig);
                TestHelper.testComplete(async);
            }
        }, error -> {
            context.fail(error);
            TestHelper.testComplete(async);
        });
    }

    protected abstract EdgeVerticle initMockupVerticle(TestContext context);

    protected void executeThenAssert(EventAction eventAction, TestContext context, JsonObject body,
                                     BiConsumer<JsonObject, Async> handler) {
        EventMessage eventMessage = EventMessage.success(eventAction, RequestData.builder().body(body).build());
        Async async = context.async();
        this.vertx.eventBus()
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
