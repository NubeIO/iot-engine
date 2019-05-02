package com.nubeiot.edge.bios;

import org.jooq.SQLDialect;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.NubeConfig.DeployConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.sql.SqlConfig;
import com.nubeiot.core.statemachine.StateMachine;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class GettingStartupModulesTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    protected Vertx vertx;
    protected EdgeVerticle testVerticle = new MockBiosVerticle(MockInitDataEntityHandler.class);
    DeploymentOptions options;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("com.nubeiot")).setLevel(Level.INFO);
        StateMachine.init();
    }

    @Before
    public void before(TestContext context) {
        SqlConfig sqlConfig = new SqlConfig();
        sqlConfig.getHikariConfig().setJdbcUrl(getJdbcUrl());
        sqlConfig.setDialect(SQLDialect.H2);

        NubeConfig nubeConfig = new NubeConfig();
        nubeConfig.setDeployConfig(new DeployConfig());

        AppConfig appConfig = new AppConfig();
        appConfig.put("__sql__", sqlConfig.toJson());

        nubeConfig.setAppConfig(appConfig);
        this.options = new DeploymentOptions().setConfig(nubeConfig.toJson());
        this.vertx = Vertx.vertx();

        Async async = context.async(1);
        this.vertx.getDelegate().deployVerticle(this.testVerticle, options, context.asyncAssertSuccess(result -> {
            TestHelper.testComplete(async);
        }));

        async.awaitSuccess();
    }

    public void after(TestContext context) {
        //this.vertx.close(context.asyncAssertSuccess());
    }

    protected @NonNull String getJdbcUrl() {
        return "jdbc:h2:file:" + folder.getRoot().toPath().resolve("dbh2local").toString();
    }

    @Test
    public void test_enabled_module(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.ENABLED, "enabled-service");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_init(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.ENABLED,
                              "pending-service-with-transaction-is-wip-prestate-action-is-init");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_create(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.ENABLED,
                              "pending-service-with-transaction-is-wip-prestate-action-is-create");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_update(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.ENABLED,
                              "pending-service-with-transaction-is-wip-prestate-action-is-update");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_patch(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.ENABLED,
                              "pending-service-with-transaction-is-wip-prestate-action-is-patch");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_update_disabled(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.DISABLED,
                              "pending-service-with-transaction-is-wip-prestate-action-is-update-disabled");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_patch_disabled(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.DISABLED,
                              "pending-service-with-transaction-is-wip-prestate-action-is-patch-disabled");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_disabled_module(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.DISABLED, "disabled-module");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_module_with_2_transactions(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.ENABLED, "pending_module_with_two_transactions");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_module_with_2_transactions_invalid(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.DISABLED,
                              "pending_module_with_two_transactions_invalid");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_pending_but_failed_module(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.testVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.DISABLED, "pending-but-failed-module");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

    @Test
    public void test_getting_startup_modules(TestContext context) {
        Async async = context.async(1);
        this.testVerticle.getEntityHandler().getModulesWhenBootstrap().subscribe(modules -> {
            System.out.println("Assert Size");
            context.assertEquals(modules.size(), 6);
            TestHelper.testComplete(async);
        }, error -> {
            context.fail();
            TestHelper.testComplete(async);
        });
        async.awaitSuccess();
    }

    private void assertModuleState(TestContext context, Async async1, TblModuleDao moduleDao, State expectedState,
                                   String moduleId) {
        moduleDao.findOneById(moduleId).subscribe(result -> {
            TblModule tblModule = result.orElse(null);
            context.assertNotNull(tblModule);
            if (tblModule.getState() != State.PENDING) {
                System.out.println("Checking state of " + moduleId);
                context.assertEquals(tblModule.getState(), expectedState);
                TestHelper.testComplete(async1);
            }
        }, error -> {
            context.fail(error);
            TestHelper.testComplete(async1);
        });
    }

}
