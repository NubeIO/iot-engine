package com.nubeiot.edge.bios.startupmodules;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseEdgeVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.PendingModuleWithUpdateActionInitData;
import com.nubeiot.edge.core.EdgeVerticle;

@RunWith(VertxUnitRunner.class)
public class PendingModuleWithUpdateActionTest extends BaseEdgeVerticleTest {

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(PendingModuleWithUpdateActionInitData.class);
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_update(TestContext context) {
        assertModuleState(context, context.async(), State.ENABLED,
                          "pending-service-with-transaction-is-wip-prestate-action-is-update");
    }

}
