package com.nubeiot.edge.bios.startupmodules;

import org.junit.Ignore;
import org.junit.Test;

import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseInstallerVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.PendingModuleWithTwoTransactionsInitData;
import com.nubeiot.edge.installer.InstallerVerticle;

@Ignore
public class PendingModuleWithTwoTransactionsTest extends BaseInstallerVerticleTest {

    @Override
    protected InstallerVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(PendingModuleWithTwoTransactionsInitData.class);
    }

    @Test
    public void test_module_with_2_transactions(TestContext context) {
        assertModuleState(context, context.async(), State.ENABLED, "pending_module_with_two_transactions");
    }

}
