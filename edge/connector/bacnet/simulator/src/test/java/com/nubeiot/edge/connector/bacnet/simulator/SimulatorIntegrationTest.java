package com.nubeiot.edge.connector.bacnet.simulator;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;
import com.serotonin.bacnet4j.util.sero.ThreadUtils;

//TODO temporary ignore
@Ignore
@RunWith(VertxUnitRunner.class)
public class SimulatorIntegrationTest {

    Vertx vertx;
    EventController eventController;
    BACnetVerticle verticle;
    JsonObject bacnetConfig;
    int remoteDeviceId;

    @BeforeClass
    public static void beforeSuite() { TestHelper.setup(); }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Before
    public void before(TestContext context) throws IOException {
        verticle = new BACnetVerticle();
        vertx = Vertx.vertx();
        eventController = new EventController(vertx);
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions(), verticle);

        bacnetConfig = IConfig.fromClasspath("simulator.json", NubeConfig.class).getAppConfig().toJson();
        remoteDeviceId = bacnetConfig.getInteger("deviceID");
        System.out.println("SIMULATOR ID: " + remoteDeviceId);
        ThreadUtils.sleep(250); //just to assure enough discovery time
    }

    @Test
    public void deviceDiscoveryTest(TestContext context) throws Exception {
        Async async = context.async();
        eventController.fire("nubeiot.edge.connector.bacnet.device", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_LIST, new JsonObject()), messageAsyncResult -> {
                EventMessage message = EventMessage.from(messageAsyncResult.result().body());
                context.assertTrue(messageAsyncResult.succeeded());
                context.assertTrue(message.getData().containsKey(Integer.toString(remoteDeviceId)));
                TestHelper.testComplete(async);
            });
    }

    @Test
    public void deviceExtendedInfoTest(TestContext context) throws Exception {
        Async async = context.async();
        eventController.fire("nubeiot.edge.connector.bacnet.device", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_ONE, new JsonObject().put("deviceID",
                                                                                            remoteDeviceId)),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.from(messageAsyncResult.result().body());
                                 JsonObject data = message.getData();
                                 context.assertTrue(messageAsyncResult.succeeded());
                                 context.assertEquals(remoteDeviceId, data.getInteger("instanceNumber"));
                                 context.assertEquals(bacnetConfig.getString("deviceName"), data.getString("name"));
                                 TestHelper.testComplete(async);
                             });
    }

}
