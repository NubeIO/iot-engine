package com.nubeiot.edge.bios.startupmodules;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseInstallerVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.PendingModuleWithPatchActionInitData;
import com.nubeiot.edge.installer.InstallerVerticle;

@RunWith(VertxUnitRunner.class)
public class PendingModuleWithPatchActionTest extends BaseInstallerVerticleTest {

    @Override
    protected InstallerVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(PendingModuleWithPatchActionInitData.class);
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_patch(TestContext context) {
        assertModuleState(context, context.async(), State.ENABLED,
                          "pending-service-with-transaction-is-wip-prestate-action-is-patch");
    }

}
