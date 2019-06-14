package com.nubeiot.edge.bios;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

@RunWith(VertxUnitRunner.class)
public class HandlerTimeoutTest extends BaseEdgeVerticleTest {

    //see mock-verticle.json to see timeout value is 5000

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
        return new MockTimeoutVerticle();
    }

    @Test
    public void test_create_should_timeout(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", DEPLOY_CONFIG);

        EventMessage eventMessage = EventMessage.success(EventAction.CREATE, RequestData.builder().body(body).build());
        Async async = context.async();
        //create loading takes 7 seconds when timeout is 5 seconds
        this.vertx.getDelegate()
                  .eventBus()
                  .send(MockTimeoutVerticle.MOCK_TIME_OUT_INSTALLER.getAddress(), eventMessage.toJson(),
                        context.asyncAssertSuccess(handle -> {
                            System.out.println(handle.body());
                            TestHelper.testComplete(async);
                            async.awaitSuccess();
                        }));

        this.testingDBUpdated(context, State.DISABLED, Status.FAILED, DEPLOY_CONFIG);
        async.awaitSuccess();
    }

    @Test
    public void test_send_request_directly_should_timeout(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", DEPLOY_CONFIG);

        EventMessage eventMessage = EventMessage.success(EventAction.CREATE, RequestData.builder().body(body).build());
        Async async = context.async();
        EventController controller = EventController.getInstance(this.vertx, this.edgeVerticle.getNubeConfig()
                                                                                              .getSystemConfig()
                                                                                              .getEventBusConfig()
                                                                                              .getDeliveryOptions());
        //create loading takes 7 seconds when timeout is 5 seconds
        controller.request(EdgeInstallerEventBus.BIOS_DEPLOYMENT.getAddress(),
                           EdgeInstallerEventBus.BIOS_DEPLOYMENT.getPattern(), eventMessage,
                           context.asyncAssertFailure(response -> {
                               context.assertTrue(response instanceof ReplyException);
                               context.assertEquals(((ReplyException) response).failureType(), ReplyFailure.TIMEOUT);
                               context.assertEquals(((ReplyException) response).failureCode(), -1);
                               TestHelper.testComplete(async);
                           }), null);

        async.awaitSuccess();
    }

}
