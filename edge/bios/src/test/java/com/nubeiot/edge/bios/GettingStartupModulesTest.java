package com.nubeiot.edge.bios;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.enums.State;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;

@RunWith(VertxUnitRunner.class)
public class GettingStartupModulesTest extends BaseEdgeVerticleTest {

    @BeforeClass
    public static void beforeSuite() {
        BaseEdgeVerticleTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) {
        super.before(context);
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(MockInitDataEntityHandler.class);
    }

    @Test
    public void test_enabled_module(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
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
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
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
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
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
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
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
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
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
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
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
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
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
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
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
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
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
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
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
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.DISABLED, "pending-but-failed-module");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
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
