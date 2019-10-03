package com.nubeiot.edge.bios.startupmodules;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseInstallerVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.EnabledModuleInitData;
import com.nubeiot.edge.core.InstallerVerticle;

@RunWith(VertxUnitRunner.class)
public class EnabledModuleTest extends BaseInstallerVerticleTest {

    @Override
    protected InstallerVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(EnabledModuleInitData.class);
    }

    @Test
    public void test_enabled_module(TestContext context) {
        assertModuleState(context, context.async(), State.ENABLED, "enabled-service");
    }

}
