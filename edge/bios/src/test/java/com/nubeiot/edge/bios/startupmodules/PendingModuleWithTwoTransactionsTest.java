package com.nubeiot.edge.bios.startupmodules;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.enums.State;
import com.nubeiot.edge.bios.BaseEdgeVerticleTest;
import com.nubeiot.edge.bios.startupmodules.handler.PendingModuleWithPatchActionInitData;
import com.nubeiot.edge.bios.startupmodules.handler.PendingModuleWithTwoTransactionsInitData;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.model.tables.daos.TblModuleDao;

@RunWith(VertxUnitRunner.class)
public class PendingModuleWithTwoTransactionsTest extends BaseEdgeVerticleTest {

    @BeforeClass
    public static void beforeSuite() {
        BaseEdgeVerticleTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) {
        super.before(context);
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosStartupModulesVerticle(PendingModuleWithTwoTransactionsInitData.class);
    }

    @Test
    public void test_module_with_2_transactions(TestContext context) {
        Async async1 = context.async(1);
        long timer = this.vertx.setPeriodic(1000, event -> {
            TblModuleDao moduleDao = this.edgeVerticle.getEntityHandler().getModuleDao();
            assertModuleState(context, async1, moduleDao, State.ENABLED, "pending_module_with_two_transactions");
        });
        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async1);
        });
        async1.awaitSuccess();
    }

}
