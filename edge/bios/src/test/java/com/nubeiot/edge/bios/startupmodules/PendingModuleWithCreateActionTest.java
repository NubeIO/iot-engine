package com.nubeiot.edge.bios.startupmodules;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseEdgeVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.PendingModuleWithCreateActionInitData;
import com.nubeiot.edge.core.EdgeVerticle;

@RunWith(VertxUnitRunner.class)
public class PendingModuleWithCreateActionTest extends BaseEdgeVerticleTest {

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(PendingModuleWithCreateActionInitData.class);
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_create(TestContext context) {
        assertModuleState(context, context.async(), State.ENABLED,
                          "pending-service-with-transaction-is-wip-prestate-action-is-create");
    }

}
