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

import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.SystemConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

@RunWith(VertxUnitRunner.class)
public class HandlerTimeoutTest extends BaseEdgeVerticleTest {

    @BeforeClass
    public static void beforeSuite() {
        BaseEdgeVerticleTest.beforeSuite();
    }

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
                                                  .setModifiedAt(DateTimes.nowUTC()));
    }

    @Override
    protected NubeConfig getNubeConfig() {
        NubeConfig config = super.getNubeConfig();
        config.setSystemConfig(new SystemConfig());
        config.getSystemConfig().getEventBusConfig().getDeliveryOptions().setSendTimeout(5000);
        return config;
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
    public void test_patch_no_time_out(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", APP_CONFIG);

        //loading patch takes 3 seconds when timeout is 5 seconds
        EventMessage eventMessage = EventMessage.success(EventAction.PATCH, RequestData.builder().body(body).build());
        Async async = context.async();
        this.vertx.eventBus()
                  .send(MockTimeoutVerticle.MOCK_TIME_OUT_INSTALLER.getAddress(), eventMessage.toJson(),
                        context.asyncAssertSuccess(handle -> {
                            System.out.println(handle.body());
                            TestHelper.testComplete(async);
                            async.awaitSuccess();
                        }));

        this.testingDBUpdated(context, State.ENABLED, Status.SUCCESS, APP_CONFIG);
        async.awaitSuccess();
    }

    @Test
    public void test_patch_directly_no_time_out(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", APP_CONFIG);

        EventMessage eventMessage = EventMessage.success(EventAction.PATCH, RequestData.builder().body(body).build());
        Async async = context.async();
        //loading patch takes 3 seconds when timeout is 5 seconds
        this.edgeVerticle.getEventController()
                         .request(EdgeInstallerEventBus.BIOS_DEPLOYMENT.getAddress(),
                                  EdgeInstallerEventBus.BIOS_DEPLOYMENT.getPattern(), eventMessage,
                                  context.asyncAssertSuccess(response -> TestHelper.testComplete(async)));

        async.awaitSuccess();
    }

    //    @Test
    //    public void test_create_should_timeout(TestContext context) {
    //        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
    //                                              .put("group_id", GROUP_ID)
    //                                              .put("version", VERSION);
    //        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", DEPLOY_CONFIG);
    //
    //        EventMessage eventMessage = EventMessage.success(EventAction.CREATE, RequestData.builder().body(body)
    //        .build());
    //        Async async = context.async();
    //        //create loading takes 7 seconds when timeout is 5 seconds
    //        this.edgeVerticle.getEventController()
    //                         .request(MockTimeoutVerticle.MOCK_TIME_OUT_INSTALLER.getAddress(),
    //                                  MockTimeoutVerticle.MOCK_TIME_OUT_INSTALLER.getPattern(), eventMessage,
    //                                  context.asyncAssertSuccess(handle -> {
    //                                      System.out.println(handle);
    //                                      TestHelper.testComplete(async);
    //                                      async.awaitSuccess();
    //                                  }), null);
    //
    //        this.testingDBUpdated(context, State.DISABLED, Status.FAILED, DEPLOY_CONFIG);
    //        async.awaitSuccess();
    //    }

    @Test
    public void test_send_request_directly_should_timeout(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", APP_CONFIG);

        EventMessage eventMessage = EventMessage.success(EventAction.CREATE, RequestData.builder().body(body).build());
        Async async = context.async();

        //create loading takes 7 seconds when timeout is 5 seconds
        this.edgeVerticle.getEventController()
                         .request(EdgeInstallerEventBus.BIOS_DEPLOYMENT.getAddress(),
                                  EdgeInstallerEventBus.BIOS_DEPLOYMENT.getPattern(), eventMessage,
                                  context.asyncAssertFailure(response -> {
                                      context.assertTrue(response instanceof ReplyException);
                                      context.assertEquals(((ReplyException) response).failureType(),
                                                           ReplyFailure.TIMEOUT);
                                      context.assertEquals(((ReplyException) response).failureCode(), -1);
                                      TestHelper.testComplete(async);
                                  }));

        async.awaitSuccess();
    }

}
