package com.nubeiot.edge.bios.startupmodules;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseEdgeVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.InvalidModulesInitData;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;

@RunWith(VertxUnitRunner.class)
public class GettingInvalidModulesTest extends BaseEdgeVerticleTest {

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(InvalidModulesInitData.class);
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

}
