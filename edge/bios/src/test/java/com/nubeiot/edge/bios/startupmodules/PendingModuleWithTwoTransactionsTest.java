package com.nubeiot.edge.bios.startupmodules;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseEdgeVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.PendingModuleWithTwoTransactionsInitData;
import com.nubeiot.edge.core.EdgeVerticle;

@RunWith(VertxUnitRunner.class)
public class PendingModuleWithTwoTransactionsTest extends BaseEdgeVerticleTest {

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(PendingModuleWithTwoTransactionsInitData.class);
    }

    @Test
    public void test_module_with_2_transactions(TestContext context) {
        assertModuleState(context, context.async(), State.ENABLED, "pending_module_with_two_transactions");
    }

}
