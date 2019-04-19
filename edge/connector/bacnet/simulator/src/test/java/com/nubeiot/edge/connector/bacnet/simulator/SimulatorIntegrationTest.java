package com.nubeiot.edge.connector.bacnet.simulator;

import java.io.IOException;

import org.junit.AfterClass;
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
import com.nubeiot.core.utils.Configs;
import com.nubeiot.edge.connector.bacnet.BACnetConfig;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;
import com.nubeiot.edge.connector.bacnet.Util.BACnetDataConversions;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.util.sero.ThreadUtils;

//TODO temporary ignore
@Ignore
@RunWith(VertxUnitRunner.class)
public class SimulatorIntegrationTest {

    static Vertx vertx;
    static EventController eventController;
    static BACnetVerticle verticle;
    static BACnetConfig simConfig;
    static BACnetConfig masterBACnetConfig;
    static JsonObject testPoints;
    static int remoteDeviceId;

    @AfterClass
    public static void after(TestContext context) {
        verticle.stop();
        vertx.close(context.asyncAssertSuccess());
    }

    @BeforeClass
    public static void beforeAll(TestContext context) throws IOException {
        TestHelper.setup();
        verticle = new BACnetMasterTest();
        vertx = Vertx.vertx();
        eventController = new EventController(vertx);
        JsonObject masterConfig = IConfig.fromClasspath("master.json", NubeConfig.class).toJson();
        masterBACnetConfig = IConfig.from(masterConfig, BACnetConfig.class);
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions().setConfig(masterConfig), verticle);
        simConfig = IConfig.fromClasspath("config.json", BACnetConfig.class);
        remoteDeviceId = simConfig.getDeviceId();
        testPoints = Configs.loadJsonConfig("points.json");
        //        System.out.println("SIMULATOR ID: " + remoteDeviceId);
        ThreadUtils.sleep(1000); //just to assure enough discovery time
    }

    @Test
    public void deviceDiscoveryTest(TestContext context) throws Exception {
        Async async = context.async();
        eventController.fire("nubeiot.edge.connector.bacnet.device", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_LIST, addNetWorkToJson(new JsonObject())),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.from(messageAsyncResult.result().body());
                                 System.out.println(message.getData());
                                 context.assertTrue(message.isSuccess());
                                 context.assertTrue(message.getData().containsKey(Integer.toString(remoteDeviceId)));
                                 TestHelper.testComplete(async);
                             });
    }

    @Test
    public void deviceExtendedInfoTest(TestContext context) throws Exception {
        Async async = context.async();
        eventController.fire("nubeiot.edge.connector.bacnet.device", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_ONE,
                                                  addNetWorkToJson(new JsonObject()).put("deviceId", remoteDeviceId)),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.from(messageAsyncResult.result().body());
                                 JsonObject data = message.getData();
                                 context.assertTrue(message.isSuccess());
                                 context.assertEquals(remoteDeviceId, data.getInteger("instanceNumber"));
                                 //                                 context.assertEquals(simConfig.getDeviceName(),
                                 //                                 data.getString("name"));
                                 TestHelper.testComplete(async);
                             });
    }

    @Test
    public void deviceExtendedInfoNotFoundExceptionTest(TestContext context) throws Exception {
        Async async = context.async();
        int id = remoteDeviceId + 1;
        eventController.fire("nubeiot.edge.connector.bacnet.device", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_ONE,
                                                  addNetWorkToJson(new JsonObject()).put("deviceId", id)),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.from(messageAsyncResult.result().body());
                                 context.assertTrue(message.isError());
                                 TestHelper.testComplete(async);
                             });
    }

    @Test
    public void readAllPoints(TestContext context) throws Exception {
        Async async = context.async();
        String address = "nubeiot.edge.connector.bacnet.device.points";
        eventController.fire(address, EventPattern.REQUEST_RESPONSE, EventMessage.initial(EventAction.GET_LIST,
                                                                                          addNetWorkToJson(
                                                                                              new JsonObject()).put(
                                                                                              "deviceId",
                                                                                              remoteDeviceId)),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.from(messageAsyncResult.result().body());
                                 JsonObject data = message.getData();
                                 context.assertTrue(message.isSuccess());
                                 context.assertFalse(data.isEmpty());
                                 testPoints.getMap()
                                           .keySet()
                                           .forEach(o -> context.assertTrue(
                                               data.containsKey(BACnetDataConversions.pointIDNubeToBACnet(o))));
                                 TestHelper.testComplete(async);
                             });
    }

    @Test
    public void readSinglePoint(TestContext context) throws Exception {
        Async async = context.async();
        String p1 = testPoints.getMap().keySet().iterator().next();
        JsonObject p1o = testPoints.getJsonObject(p1);
        eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_ONE,
                                                  addNetWorkToJson(new JsonObject()).put("deviceId", remoteDeviceId)
                                                                                    .put("objectId",
                                                                                         BACnetDataConversions.pointIDNubeToBACnet(
                                                                                             p1))),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.from(messageAsyncResult.result().body());
                                 context.assertTrue(message.isSuccess());

                                 Object val = p1o.getValue("value");
                                 if (val instanceof Integer) {
                                     val = new Float((Integer) val);
                                 }
                                 context.assertEquals(val, message.getData()
                                                                  .getValue(
                                                                      PropertyIdentifier.presentValue.toString()));
                                 TestHelper.testComplete(async);
                             });
    }

    @Test
    public void writePointTest(TestContext context) throws Exception {
        Async async = context.async();
        String pointId = BACnetDataConversions.pointIDNubeToBACnet("R1");
        JsonObject point = testPoints.getJsonObject("R1");
        boolean val = point.getInteger("value") == 1;
        JsonObject initMessage = addNetWorkToJson(new JsonObject()).put("deviceId", remoteDeviceId)
                                                                   .put("objectId", pointId)
                                                                   .put("priority", 16)
                                                                   .put("value", !val);

        System.out.println("point:      " + pointId);
        System.out.println("CurrentVal: " + val);
        System.out.println("NewVal:     " + !val);

        eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.PATCH, initMessage), messageAsyncResult -> {

                context.assertTrue(EventMessage.from(messageAsyncResult.result().body()).isSuccess());
                eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                                     EventMessage.initial(EventAction.GET_ONE,
                                                          addNetWorkToJson(new JsonObject()).put("deviceId",
                                                                                                 remoteDeviceId)
                                                                                            .put("objectId", pointId)),
                                     messageAsyncResult2 -> {
                                         EventMessage message2 = EventMessage.from(messageAsyncResult2.result().body());
                                         context.assertTrue(message2.isSuccess());
                                         int expected = val ? 0 : 1;
                                         context.assertEquals(expected, message2.getData()
                                                                                .getValue(
                                                                                    PropertyIdentifier.presentValue.toString()));
                                         TestHelper.testComplete(async);
                                     });
            });
    }

    @Test
    public void subscribeSuccessTest(TestContext context) throws Exception {
        Async async = context.async();

        eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.CREATE,
                                                  addNetWorkToJson(new JsonObject()).put("deviceId", remoteDeviceId)
                                                                                    .put("objectId", "analog-input:1")
                                                                                    .put("pollSeconds", 0)),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.from(messageAsyncResult.result().body());
                                 context.assertTrue(message.isSuccess());
                                 context.assertTrue(message.getData().containsKey("saveType"));
                                 context.assertEquals("COV", message.getData().getString("saveType"));

                                 TestHelper.testComplete(async);
                             });
    }

    private JsonObject addNetWorkToJson(JsonObject json) {
        return json.put("network", masterBACnetConfig.getIpConfigs().iterator().next().getName());
    }

}
