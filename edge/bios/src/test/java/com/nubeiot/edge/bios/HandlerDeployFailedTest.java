package com.nubeiot.edge.bios;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.core.EdgeVerticle;

@RunWith(VertxUnitRunner.class)
public class HandlerDeployFailedTest extends BaseEdgeVerticleTest {

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
        return new MockBiosEdgeVerticle(this.getConsumer(context), true);
    }

    @Test
    public void test_deploy_failed(TestContext context) {
        JsonObject appConfig = new JsonObject().put("", "");
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("state", State.ENABLED)
                                              .put("version", VERSION)
                                              .put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", appConfig);
        executeThenAssert(EventAction.CREATE, context, body, (response, async) -> {
            TestHelper.testComplete(async);
            async.awaitSuccess();
        });
        testingDBUpdated(context, State.DISABLED, Status.FAILED, appConfig);
    }

}
