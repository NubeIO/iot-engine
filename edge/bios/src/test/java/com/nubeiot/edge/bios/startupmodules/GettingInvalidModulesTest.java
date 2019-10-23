package com.nubeiot.edge.bios.startupmodules;

import org.junit.Test;

import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseInstallerVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.InvalidModulesInitData;
import com.nubeiot.edge.bios.startupmodules.mock.MockBiosStartupModulesVerticle;
import com.nubeiot.edge.installer.InstallerVerticle;

/**
 * It basically updates the Table > Module to DISABLE state via searching the Table > Transaction state which is WIP and
 * then searches for prev_meta_data of Table > Module
 */
public class GettingInvalidModulesTest extends BaseInstallerVerticleTest {

    @Override
    protected InstallerVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(InvalidModulesInitData.class);
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_update_disabled(TestContext context) {
        assertModuleState(context, context.async(), State.DISABLED,
                          "pending-service-with-transaction-is-wip-prestate-action-is-update-disabled");
    }

    @Test
    public void test_pending_service_with_transaction_is_wip_prestate_action_is_patch_disabled(TestContext context) {
        assertModuleState(context, context.async(), State.DISABLED,
                          "pending-service-with-transaction-is-wip-prestate-action-is-patch-disabled");
    }

    @Test
    public void test_disabled_module(TestContext context) {
        assertModuleState(context, context.async(), State.DISABLED, "disabled-module");
    }

    @Test
    public void test_module_with_2_transactions_invalid(TestContext context) {
        assertModuleState(context, context.async(), State.DISABLED, "pending_module_with_two_transactions_invalid");
    }

}
