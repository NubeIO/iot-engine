package com.nubeiot.edge.bios;

import java.util.Collections;

import org.jooq.SQLDialect;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.reactivex.core.Vertx;

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
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    protected Vertx vertx;
    protected EdgeVerticle edgeVerticle;

    protected static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.ERROR);
        StateMachine.init();
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
        this.vertx.getDelegate().deployVerticle(this.edgeVerticle, options, context.asyncAssertSuccess(result -> {
            TestHelper.testComplete(async);
        }));
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
            EventMessage moduleMessage = EventMessage.success(EventAction.GET_ONE, RequestData.builder()
                .body(
                    new JsonObject().put(
                        "service_id",
                        preDeploymentResult
                            .getServiceId()))
                .build());

            EventMessage transactionMessage = EventMessage.success(EventAction.GET_ONE, RequestData.builder()
                .body(
                    new JsonObject().put(
                        "transaction_id",
                        preDeploymentResult
                            .getTransactionId()))
                .build());
            final Async async = context.async(2);

            this.vertx.getDelegate()
                .eventBus()
                .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), moduleMessage.toJson(), result -> {
                    System.out.println("Asserting module");
                    JsonObject body = (JsonObject) result.result().body();
                    context.assertEquals(body.getString("status"), Status.SUCCESS.name());
                    JsonObject data = body.getJsonObject("data");
                    context.assertEquals(data.getString("state"), State.PENDING.name());
                    TestHelper.testComplete(async);
                });

            this.vertx.getDelegate()
                .eventBus()
                .send(EdgeInstallerEventBus.BIOS_TRANSACTION.getAddress(), transactionMessage.toJson(), result -> {
                    System.out.println("Asserting transaction");
                    JsonObject body = (JsonObject) result.result().body();
                    context.assertEquals(body.getString("status"), Status.SUCCESS.name());
                    JsonObject data = body.getJsonObject("data");
                    context.assertEquals(data.getString("status"), Status.WIP.name());
                    TestHelper.testComplete(async);
                });
            //async.awaitSuccess();
        };
    }

    protected void testingDBUpdated(TestContext context, String moduleId, State expectedModuleState,
                                    Status expectedTransactionStatus) {
        Async async = context.async(2);
        //Event module is deployed/updated successfully, we still have a gap for DB update.
        long timer = this.vertx.setPeriodic(1000, event -> {
            edgeVerticle.getEntityHandler()
                .getModuleDao()
                .findOneById(moduleId)
                .subscribe(result -> {
                    TblModule tblModule = result.orElse(null);
                    context.assertNotNull(tblModule);
                    if (tblModule.getState() != State.PENDING) {
                        System.out.println("Ready. Testing module");
                        context.assertEquals(tblModule.getState(), expectedModuleState);
                        TestHelper.testComplete(async);
                    }
                }, error -> {
                    context.fail(error);
                    TestHelper.testComplete(async);
                });
            edgeVerticle.getEntityHandler()
                .getTransDao()
                .findManyByModuleId(Collections.singletonList(moduleId))
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
            TestHelper.testComplete(async);
        });
    }

    protected abstract EdgeVerticle initMockupVerticle(TestContext context);

}
