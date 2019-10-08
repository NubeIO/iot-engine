package com.nubeiot.edge.bios.startupmodules;

import org.junit.Ignore;
import org.junit.Test;

import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseInstallerVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.PendingModuleWithCreateActionInitData;
import com.nubeiot.edge.installer.InstallerVerticle;

@Ignore
public class PendingModuleWithCreateActionTest extends BaseInstallerVerticleTest {

    @Override
    protected InstallerVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(PendingModuleWithCreateActionInitData.class);
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_create(TestContext context) {
        assertModuleState(context, context.async(), State.ENABLED,
                          "pending-service-with-transaction-is-wip-prestate-action-is-create");
    }

}
