package com.nubeiot.edge.bios;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.SystemConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

@RunWith(VertxUnitRunner.class)
public class HandlerTimeoutTest extends BaseEdgeVerticleTest {

    @Before
    public void before(TestContext context) {
        super.before(context);
        this.insertModule(context, new TblModule().setServiceId(MODULE_ID)
                                                  .setServiceType(ModuleType.JAVA)
                                                  .setServiceName(SERVICE_NAME)
                                                  .setState(State.ENABLED)
                                                  .setVersion(VERSION)
                                                  .setAppConfig(APP_CONFIG)
                                                  .setSystemConfig(APP_SYSTEM_CONFIG)
                                                  .setModifiedAt(DateTimes.now()));
    }

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockTimeoutVerticle();
    }

    @Override
    protected NubeConfig getNubeConfig() {
        NubeConfig config = super.getNubeConfig();
        config.setSystemConfig(new SystemConfig());
        config.getSystemConfig().getEventBusConfig().getDeliveryOptions().setSendTimeout(3000);
        return config;
    }

    @Test
    public void test_patch_no_time_out(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", APP_CONFIG);

        Async async = context.async();
        //loading patch takes 1 seconds when timeout is 3 seconds
        final JsonObject expectedBody = new JsonObject(
            "{\"action\":\"PATCH\",\"message\":\"Work in progress\",\"prev_state\":\"ENABLED\"," +
            "\"service_fqn\":\"maven:com.nubeiot.edge.module:mytest:1.0.0::bios-mytest\",\"status\":\"WIP\",\"silent" +
            "\":false,\"target_state\":\"ENABLED\",\"service_id\":\"com.nubeiot.edge.module:mytest\"}");
        final JsonObject expected = new JsonObject().put("status", Status.SUCCESS)
                                                    .put("action", EventAction.PATCH)
                                                    .put("data", expectedBody);
        this.edgeVerticle.getEventController()
                         .request(DeliveryEvent.from(MockTimeoutVerticle.MOCK_TIME_OUT_INSTALLER, EventAction.PATCH,
                                                     RequestData.builder().body(body).build().toJson()),
                                  EventbusHelper.replyAsserter(context, async, expected,
                                                               JsonHelper.ignore("data.transaction_id"),
                                                               JsonHelper.ignore("data.system_config")));
        this.testingDBUpdated(context, State.ENABLED, Status.SUCCESS, APP_CONFIG);
    }

    @Test
    public void test_patch_directly_no_time_out(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", APP_CONFIG);

        Async async = context.async();
        //loading patch takes 1 seconds when timeout is 3 seconds
        final JsonObject expected = new JsonObject().put("status", Status.SUCCESS)
                                                    .put("action", EventAction.PATCH)
                                                    .put("data", new JsonObject("{\"abc\":\"123\"}"));
        this.edgeVerticle.getEventController()
                         .request(DeliveryEvent.from(InstallerEventModel.BIOS_DEPLOYMENT, EventAction.PATCH,
                                                     RequestData.builder().body(body).build().toJson()),
                                  EventbusHelper.replyAsserter(context, async, expected));
    }

    @Test
    public void test_send_request_directly_should_timeout(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", APP_CONFIG);
        Async async = context.async();
        final DeliveryEvent deliveryEvent = DeliveryEvent.from(InstallerEventModel.BIOS_DEPLOYMENT, EventAction.CREATE,
                                                               RequestData.builder().body(body).build().toJson());
        //create loading takes 9 seconds when timeout is 3 seconds
        this.edgeVerticle.getEventController().request(deliveryEvent, context.asyncAssertFailure(throwable -> {
            context.assertTrue(throwable instanceof ReplyException);
            context.assertEquals(((ReplyException) throwable).failureType(), ReplyFailure.TIMEOUT);
            context.assertEquals(((ReplyException) throwable).failureCode(), -1);
            TestHelper.testComplete(async);
        }));
    }

}
