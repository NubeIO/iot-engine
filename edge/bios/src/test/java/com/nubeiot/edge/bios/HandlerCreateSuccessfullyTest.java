package com.nubeiot.edge.bios;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.edge.core.EdgeVerticle;

@RunWith(VertxUnitRunner.class)
public class HandlerCreateSuccessfullyTest extends BaseEdgeVerticleTest {

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
        return new MockBiosEdgeVerticle(this.getConsumer(context));
    }

    @Test
    public void test_create_success(TestContext context) {
        JsonObject deployConfig = new JsonObject().put("", "");
        EventMessage eventMessage = EventMessage.success(EventAction.CREATE, RequestData.builder()
                                                                                        .body(new JsonObject().put(
                                                                                            "artifact_id", ARTIFACT_ID)
                                                                                                              .put(
                                                                                                                  "group_id",
                                                                                                                  GROUP_ID)
                                                                                                              .put(
                                                                                                                  "version",
                                                                                                                  VERSION)
                                                                                                              .put(
                                                                                                                  "deploy_config",
                                                                                                                  deployConfig))
                                                                                        .build());
        Async async = context.async();
        this.vertx.getDelegate()
                  .eventBus()
                  .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                        context.asyncAssertSuccess(handle -> {
                            TestHelper.testComplete(async);
                        }));

        async.awaitSuccess();
        //Checking module state and transaction status
        testingDBUpdated(context, MODULE_ID, State.ENABLED, Status.SUCCESS);
    }

}
