package com.nubeiot.edge.bios.startupmodules;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseEdgeVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.PendingModuleWithPatchActionInitData;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;

@RunWith(VertxUnitRunner.class)
public class PendingModuleWithPatchActionTest extends BaseEdgeVerticleTest {

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(PendingModuleWithPatchActionInitData.class);
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

}
