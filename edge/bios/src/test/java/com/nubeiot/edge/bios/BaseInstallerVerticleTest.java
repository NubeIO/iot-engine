package com.nubeiot.edge.bios;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jooq.SQLDialect;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.NubeConfig.DeployConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.sql.SqlConfig;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.edge.bios.service.BiosModuleService;
import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;

import lombok.NonNull;

/**
 * {@link HandlerCreateTest#test_create_should_success(TestContext)}
 */
@RunWith(VertxUnitRunner.class)
//FIXME: MUST REWRITE/REFACTOR SHIT CODE. IT IS ASSERTING SUCCESS EVEN RAISE EXCEPTION
public abstract class BaseInstallerVerticleTest {

    protected static final String GROUP_ID = "com.nubeiot.edge.module";
    protected static final String ARTIFACT_ID = "mytest";
    protected static final String VERSION = "1.0.0";
    protected static final String SERVICE_NAME = "bios-mytest";
    protected static final String MODULE_ID = GROUP_ID + ":" + ARTIFACT_ID;
    protected static final JsonObject APP_CONFIG = new JsonObject(
        "{\"__kafka__\":{\"__client__\":{\"bootstrap.servers\":[\"localhost:9092\"]}}," +
        "\"__sql__\":{\"dialect\":\"H2\",\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:file:" +
        "./bios-installer\",\"minimumIdle\":1,\"maximumPoolSize\":2," +
        "\"connectionTimeout\":30000,\"idleTimeout\":180000,\"maxLifetime\":300000}}}");
    protected static final JsonObject APP_SYSTEM_CONFIG = new JsonObject(
        "{\"__deploy__\":{\"ha\":false,\"instances\":1,\"maxWorkerExecuteTime\":60000000000," +
        "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\",\"multiThreaded\":false,\"worker\":false," +
        "\"workerPoolSize\":20},\"dataDir\":\"file:///root/.nubeio/com.nubeiot.edge.module_installer\"}");
    private static volatile boolean isAvailable;
    protected Vertx vertx;
    protected InstallerVerticle installerVerticle;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        if (!isAvailable) {
            StateMachine.init();
            isAvailable = true;
        }
    }

    @Before
    public void before(TestContext context) {
        DeploymentOptions options = new DeploymentOptions().setConfig(getNubeConfig().toJson());
        Async async = context.async();
        this.vertx = Vertx.vertx();
        this.installerVerticle = initMockupVerticle(context);
        this.vertx.deployVerticle(this.installerVerticle, options,
                                  context.asyncAssertSuccess(result -> TestHelper.testComplete(async)));
        async.awaitSuccess();
    }

    @After
    public void after(TestContext context) {
        this.vertx.close();
    }

    protected abstract InstallerVerticle initMockupVerticle(TestContext context);

    protected NubeConfig getNubeConfig() {
        SqlConfig sqlConfig = new SqlConfig();
        sqlConfig.getHikariConfig().setJdbcUrl(getJdbcUrl());
        sqlConfig.setDialect(SQLDialect.H2);

        NubeConfig nubeConfig = new NubeConfig();
        nubeConfig.setDeployConfig(new DeployConfig());

        AppConfig appConfig = new AppConfig();
        appConfig.put("__sql__", sqlConfig.toJson());
        appConfig.put("__micro__", new JsonObject(
            "{\"__gateway__\":{\"enabled\":false},\"__serviceDiscovery__\":{\"enabled\":false}," +
            "\"__localServiceDiscovery__\":{\"enabled\":false},\"__circuitBreaker__\":{\"enabled\":false}}"));

        nubeConfig.setAppConfig(appConfig);
        return nubeConfig;
    }

    protected void insertModule(TestContext context, TblModule module) {
        Async async = context.async(1);
        installerVerticle.getEntityHandler().moduleDao().insert(module).subscribe(result -> {
            System.out.println("Insert module successfully!");
            TestHelper.testComplete(async);
        }, error -> {
            context.fail(error);
            TestHelper.testComplete(async);
        });
        async.awaitSuccess();
    }

    private @NonNull String getJdbcUrl() {
        return "jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString();
    }

    protected void testingDBUpdated(TestContext context, State expectedModuleState, Status expectedTransactionStatus,
                                    JsonObject expectedConfig) {
        CountDownLatch latch = new CountDownLatch(2);
        Async async = context.async(2);
        //Event module is deployed/updated successfully, we still have a gap for DB update.
        long timer = this.vertx.setPeriodic(1000, event -> {
            assertModule(context, expectedModuleState, expectedConfig, async, latch);
            assertTransaction(context, expectedTransactionStatus, async, latch);
        });
        stopTimer(context, latch, timer);
    }

    private void assertTransaction(TestContext context, Status expectedTransactionStatus, Async async,
                                   CountDownLatch latch) {
        installerVerticle.getEntityHandler().transDao()
                         .findManyByModuleId(Collections.singletonList(BaseInstallerVerticleTest.MODULE_ID))
                         .subscribe(result -> {
                        context.assertNotNull(result);
                        context.assertFalse(result.isEmpty());
                        context.assertEquals(result.size(), 1);
                        if (result.get(0).getStatus() != Status.WIP) {
                            latch.countDown();
                            System.out.println("Ready. Testing transaction");
                            context.assertEquals(result.get(0).getStatus(), expectedTransactionStatus);
                            TestHelper.testComplete(async);
                        }
                    }, error -> {
                        latch.countDown();
                        context.fail(error);
                        TestHelper.testComplete(async);
                    });
    }

    private void assertModule(TestContext context, State expectedModuleState, JsonObject expectedConfig, Async async,
                              CountDownLatch latch) {
        installerVerticle.getEntityHandler().moduleDao()
                         .findOneById(BaseInstallerVerticleTest.MODULE_ID)
                         .subscribe(result -> {
            TblModule tblModule = result.orElse(null);
            context.assertNotNull(tblModule);
            if (tblModule.getState() != State.PENDING) {
                latch.countDown();
                System.out.println("Ready. Testing module");
                context.assertEquals(tblModule.getState(), expectedModuleState);
                JsonObject actualConfig = IConfig.from(tblModule.getAppConfig(), AppConfig.class).toJson();
                JsonHelper.assertJson(context, async, expectedConfig, actualConfig, JSONCompareMode.STRICT);
                TestHelper.testComplete(async);
            }
        }, error -> {
            latch.countDown();
            context.fail(error);
            TestHelper.testComplete(async);
        });
    }

    void executeThenAssert(EventAction action, TestContext context, JsonObject body, Handler<JsonObject> handler) {
        installerVerticle.getEventController()
                    .request(DeliveryEvent.from(BiosModuleService.class.getName(), EventPattern.REQUEST_RESPONSE, action,
                                                RequestData.builder().body(body).build().toJson()),
                             EventbusHelper.replyAsserter(context, handler));
    }

    protected void assertModuleState(TestContext context, Async async, State expectedState, String moduleId) {
        final TblModuleDao moduleDao = this.installerVerticle.getEntityHandler().moduleDao();
        CountDownLatch latch = new CountDownLatch(1);
        long timer = this.vertx.setPeriodic(1000, event -> moduleDao.findOneById(moduleId).subscribe(result -> {
            TblModule tblModule = result.orElse(null);
            context.assertNotNull(tblModule);
            if (tblModule.getState() != State.PENDING) {
                System.out.println("Checking state of " + moduleId);
                if (Objects.nonNull(expectedState)) {
                    context.assertEquals(tblModule.getState(), expectedState);
                }
                latch.countDown();
                TestHelper.testComplete(async);
            }
        }, error -> {
            latch.countDown();
            context.fail(error);
            TestHelper.testComplete(async);
        }));
        stopTimer(context, latch, timer);
    }

    void stopTimer(TestContext context, CountDownLatch latch, long timer) {
        try {
            context.assertTrue(latch.await(TestHelper.TEST_TIMEOUT_SEC, TimeUnit.SECONDS), "Timeout");
            vertx.cancelTimer(timer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
