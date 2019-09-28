package com.nubeiot.edge.connector.bacnet.simulator;

import java.io.IOException;
import java.util.Set;

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
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.utils.Configs;
import com.nubeiot.edge.connector.bacnet.BACnetConfig;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;
import com.nubeiot.edge.connector.bacnet.utils.BACnetDataConversions;
import com.serotonin.bacnet4j.util.sero.ThreadUtils;

//temporary ignore
@Ignore
@RunWith(VertxUnitRunner.class)
public class SimulatorIntegrationTest {

    static Vertx vertx;
    static EventController eventController;
    static BACnetVerticle verticle;
    static BACnetConfig simConfig;
    static BACnetConfig masterBACnetConfig;
    static JsonObject testPoints;
    static Integer remoteDeviceId;

    @AfterClass
    public static void after(TestContext context) throws Exception {
        verticle.stop();
        vertx.close(context.asyncAssertSuccess());
    }

    @BeforeClass
    public static void beforeAll(TestContext context) throws IOException {
        TestHelper.setup();
        verticle = new BACnetSimulator();
        vertx = Vertx.vertx();
        eventController = SharedDataDelegate.getEventController(vertx.getDelegate(),
                                                                SimulatorIntegrationTest.class.getName());
        JsonObject masterConfig = IConfig.fromClasspath("master.json", NubeConfig.class).toJson();
        masterBACnetConfig = IConfig.from(masterConfig, BACnetConfig.class);
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions().setConfig(masterConfig), verticle);
        simConfig = IConfig.fromClasspath("config.json", BACnetConfig.class);
        remoteDeviceId = simConfig.getDeviceId();
        testPoints = Configs.loadJsonConfig("points.json");
        ThreadUtils.sleep(1000); //just to assure enough discovery time
    }

    @Test
    public void deviceDiscoveryTest(TestContext context) throws Exception {
        Async async = context.async();
        eventController.fire("nubeiot.edge.connector.bacnet.device", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_LIST, addNetworkToJson(new JsonObject())),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.tryParse(messageAsyncResult.result().body(), true);
                                 context.assertTrue(message.isSuccess());
                                 context.assertTrue(message.getData().containsKey(Integer.toString(remoteDeviceId)));
                                 TestHelper.testComplete(async);
                             }, null);
    }

    @Test
    public void deviceExtendedInfoTest(TestContext context) throws Exception {
        Async async = context.async();
        eventController.fire("nubeiot.edge.connector.bacnet.device", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_ONE,
                                                  addNetworkToJson(new JsonObject()).put("deviceId",
                                                                                         remoteDeviceId.toString())),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.tryParse(messageAsyncResult.result().body(), true);
                                 JsonObject data = message.getData();
                                 context.assertTrue(message.isSuccess());
                                 context.assertEquals(remoteDeviceId, data.getInteger("instanceNumber"));
                                 TestHelper.testComplete(async);
                             }, null);
    }

    @Test
    public void deviceExtendedInfoNotFoundExceptionTest(TestContext context) throws Exception {
        Async async = context.async();
        Integer id = remoteDeviceId + 1;
        eventController.fire("nubeiot.edge.connector.bacnet.device", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_ONE,
                                                  addNetworkToJson(new JsonObject()).put("deviceId", id.toString())),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.tryParse(messageAsyncResult.result().body());
                                 context.assertTrue(message.isError());
                                 TestHelper.testComplete(async);
                             }, null);
    }

    @Test
    public void readAllPoints(TestContext context) throws Exception {
        Async async = context.async();
        String address = "nubeiot.edge.connector.bacnet.device.points-info";

        RequestData req = RequestData.builder()
                                     .body(
                                         addNetworkToJson(new JsonObject()).put("deviceId", remoteDeviceId.toString()))
                                     .build();

        eventController.fire(address, EventPattern.REQUEST_RESPONSE, EventMessage.initial(EventAction.GET_LIST, req),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.tryParse(messageAsyncResult.result().body());
                                 JsonObject data = message.getData();

                                 context.assertTrue(message.isSuccess());
                                 context.assertFalse(data.isEmpty());
                                 testPoints.getMap().keySet().forEach(o -> {
                                     try {
                                         context.assertTrue(
                                             data.containsKey(BACnetDataConversions.pointIDNubeToBACnet(o)));
                                     } catch (Exception e) {
                                         if (!e.getMessage().equalsIgnoreCase("Invalid Nube point Id")) {
                                             context.fail(e);
                                         }
                                     }
                                 });
                                 //                                 TestHelper.testComplete(async);
                                 RequestData req2 = RequestData.builder()
                                                               .body(addNetWorkToJson(new JsonObject()).put("deviceId",
                                                                                                            remoteDeviceId
                                                                                                                .toString())
                                                                                                       .put("allData",
                                                                                                            "1"))
                                                               .build();

                                 eventController.fire(address, EventPattern.REQUEST_RESPONSE,
                                                      EventMessage.initial(EventAction.GET_LIST, req2),
                                                      messageAsyncResult2 -> {
                                                          EventMessage message2 = EventMessage.tryParse(
                                                              messageAsyncResult2.result().body());
                                                          JsonObject data2 = message2.getData();

                                                          context.assertTrue(message2.isSuccess());
                                                          context.assertFalse(data2.isEmpty());
                                                          testPoints.getMap().keySet().forEach(o -> {
                                                              try {
                                                                  context.assertTrue(data2.containsKey(
                                                                      BACnetDataConversions.pointIDNubeToBACnet(o)));
                                                              } catch (Exception e) {
                                                                  if (!e.getMessage()
                                                                        .equalsIgnoreCase("Invalid Nube point Id")) {
                                                                      context.fail(e);
                                                                  }
                                                              }
                                                          });
                                                          TestHelper.testComplete(async);
                                                      }, null);
                             }, null);
    }

    @Test
    public void readSinglePointInfo(TestContext context) throws Exception {
        Async async = context.async();
        //        String p1 = testPoints.getMap().keySet().iterator().next();
        String p1 = "UO1";
        JsonObject p1o = testPoints.getJsonObject(p1);

        RequestData req = RequestData.builder()
                                     .body(addNetworkToJson(new JsonObject()).put("deviceId", remoteDeviceId.toString())
                                                                             .put("objectId",
                                                                                  BACnetDataConversions.pointIDNubeToBACnet(
                                                                                      p1)))
                                     .build();

        eventController.fire("nubeiot.edge.connector.bacnet.device.points-info", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_ONE, req), messageAsyncResult -> {
                EventMessage message = EventMessage.tryParse(messageAsyncResult.result().body());
                context.assertTrue(message.isSuccess());

                Object val = p1o.getValue("value");
                if (val instanceof Integer) {
                    val = new Float((Integer) val);
                }
                context.assertEquals(val, message.getData().getValue("present-value"));
                TestHelper.testComplete(async);
            }, null);
    }

    @Test
    public void readSinglePointNotFound(TestContext context) throws Exception {
        Async async = context.async();
        String p1 = "UI991";
        eventController.fire("nubeiot.edge.connector.bacnet.device.points-info", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_ONE, RequestData.builder()
                                                                                  .body(
                                                                                      addNetworkToJson(new JsonObject())
                                                                                          .put("deviceId",
                                                                                               remoteDeviceId.toString())
                                                                                          .put("objectId",
                                                                                               BACnetDataConversions.pointIDNubeToBACnet(
                                                                                                   p1)))
                                                                                  .build()), messageAsyncResult -> {
                EventMessage message = EventMessage.tryParse(messageAsyncResult.result().body());
                context.assertTrue(message.isError());
                TestHelper.testComplete(async);
            }, null);
    }

    @Test
    public void readSinglePointValue(TestContext context) throws Exception {
        Async async = context.async();
        String p1 = testPoints.getMap().keySet().iterator().next();
        JsonObject p1o = testPoints.getJsonObject(p1);

        RequestData req = RequestData.builder()
                                     .body(addNetworkToJson(new JsonObject()).put("deviceId", remoteDeviceId.toString())
                                                                             .put("objectId",
                                                                                  BACnetDataConversions.pointIDNubeToBACnet(
                                                                                      p1)))
                                     .build();

        eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.GET_ONE, req), messageAsyncResult -> {
                EventMessage message = EventMessage.tryParse(messageAsyncResult.result().body());
                context.assertTrue(message.isSuccess());

                Object val = p1o.getValue("value");
                if (val instanceof Integer) {
                    val = new Float((Integer) val);
                }
                context.assertEquals(val, message.getData().getValue("value"));
                TestHelper.testComplete(async);
            }, null);
    }

    @Test
    public void readMultiplePointsValue(TestContext context) throws Exception {
        Async async = context.async();
        String address = "nubeiot.edge.connector.bacnet.device.points";

        String objectIds = "";
        Set<String> keys = testPoints.getMap().keySet();
        for (String s : keys) {
            try {
                objectIds += BACnetDataConversions.pointIDNubeToBACnet(s) + ",";
            } catch (Exception e) {
            }
        }

        RequestData req = RequestData.builder()
                                     .body(
                                         addNetworkToJson(new JsonObject()).put("deviceId", remoteDeviceId.toString()))
                                     .filter(new JsonObject().put("objectIds", objectIds))
                                     .build();

        eventController.fire(address, EventPattern.REQUEST_RESPONSE, EventMessage.initial(EventAction.GET_LIST, req),
                             messageAsyncResult -> {
                                 EventMessage message = EventMessage.tryParse(messageAsyncResult.result().body());
                                 JsonObject data = message.getData();
                                 context.assertTrue(message.isSuccess());
                                 context.assertFalse(data.isEmpty());
                                 testPoints.getMap().keySet().forEach(o -> {
                                     try {
                                         context.assertTrue(data.getJsonObject("points")
                                                                .containsKey(
                                                                    BACnetDataConversions.pointIDNubeToBACnet(o)));
                                         context.assertEquals(testPoints.getJsonObject(o).getFloat("value"),
                                                              data.getJsonObject("points")
                                                                  .getFloat(
                                                                      BACnetDataConversions.pointIDNubeToBACnet(o)));
                                     } catch (Exception e) {
                                         if (!e.getMessage().equalsIgnoreCase("Invalid Nube point Id")) {
                                             context.fail(e);
                                         }
                                     }
                                 });
                                 TestHelper.testComplete(async);
                             }, null);
    }

    @Test
    public void writePointTest(TestContext context) throws Exception {
        Async async = context.async();
        String pointId = BACnetDataConversions.pointIDNubeToBACnet("R1");
        JsonObject point = testPoints.getJsonObject("R1");
        boolean val = point.getInteger("value") == 1;
        RequestData req = RequestData.builder()
                                     .body(addNetworkToJson(new JsonObject()).put("deviceId", remoteDeviceId.toString())
                                                                             .put("objectId", pointId)
                                                                             .put("priority", "16")
                                                                             .put("value", !val))
                                     .build();

        eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.PATCH, req), messageAsyncResult -> {

                context.assertTrue(EventMessage.tryParse(messageAsyncResult.result().body()).isSuccess());
                eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                                     EventMessage.initial(EventAction.GET_ONE, RequestData.builder()
                                                                                          .body(addNetworkToJson(
                                                                                              new JsonObject()).put(
                                                                                              "deviceId",
                                                                                              remoteDeviceId.toString())
                                                                                                               .put(
                                                                                                                   "objectId",
                                                                                                                   pointId))
                                                                                          .build()),
                                     messageAsyncResult2 -> {
                                         EventMessage message2 = EventMessage.tryParse(
                                             messageAsyncResult2.result().body());
                                         context.assertTrue(message2.isSuccess());
                                         int expected = val ? 0 : 1;
                                         context.assertEquals(expected, message2.getData().getValue("value"));

                                         //RESET
                                         req.body().remove("value");
                                         req.body().put("value", val);
                                         eventController.fire("nubeiot.edge.connector.bacnet.device.points",
                                                              EventPattern.REQUEST_RESPONSE,
                                                              EventMessage.initial(EventAction.PATCH, req), m -> {},
                                                              null);

                                         TestHelper.testComplete(async);
                                     }, null);
            }, null);
    }

    @Test
    public void writePointVirtualBinaryTest(TestContext context) throws Exception {
        Async async = context.async();
        String pointId = "binary-value:0";
        boolean val = true;
        RequestData req = RequestData.builder()
                                     .body(addNetworkToJson(new JsonObject()).put("deviceId", remoteDeviceId.toString())
                                                                             .put("objectId", pointId)
                                                                             .put("priority", "16")
                                                                             .put("value", !val))
                                     .build();

        eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.PATCH, req), messageAsyncResult -> {

                context.assertTrue(EventMessage.tryParse(messageAsyncResult.result().body()).isSuccess());
                eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                                     EventMessage.initial(EventAction.GET_ONE, RequestData.builder()
                                                                                          .body(addNetworkToJson(
                                                                                              new JsonObject()).put(
                                                                                              "deviceId",
                                                                                              remoteDeviceId.toString())
                                                                                                               .put(
                                                                                                                   "objectId",
                                                                                                                   pointId))
                                                                                          .build()),
                                     messageAsyncResult2 -> {
                                         EventMessage message2 = EventMessage.tryParse(
                                             messageAsyncResult2.result().body());
                                         context.assertTrue(message2.isSuccess());
                                         int expected = val ? 0 : 1;
                                         context.assertEquals(expected, message2.getData().getValue("value"));

                                         //RESET
                                         req.body().remove("value");
                                         req.body().put("value", val);
                                         eventController.fire("nubeiot.edge.connector.bacnet.device.points",
                                                              EventPattern.REQUEST_RESPONSE,
                                                              EventMessage.initial(EventAction.PATCH, req), m -> {},
                                                              null);

                                         TestHelper.testComplete(async);
                                     }, null);
            }, null);
    }

    @Test
    public void writePointNullTestBinary(TestContext context) throws Exception {
        Async async = context.async();
        String pointId = BACnetDataConversions.pointIDNubeToBACnet("R1");
        JsonObject point = testPoints.getJsonObject("R1");
        boolean val = point.getInteger("value") == 1;
        RequestData req = RequestData.builder()
                                     .body(addNetworkToJson(new JsonObject()).put("deviceId", remoteDeviceId.toString())
                                                                             .put("objectId", pointId)
                                                                             .put("priority", "10")
                                                                             .put("value", !val))
                                     .build();

        eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.PATCH, req), messageAsyncResult -> {

                context.assertTrue(EventMessage.tryParse(messageAsyncResult.result().body()).isSuccess());
                req.body().remove("value");
                req.body().put("value", "null");

                eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                                     EventMessage.initial(EventAction.PATCH, req), messageAsyncResult2 -> {

                        context.assertTrue(EventMessage.tryParse(messageAsyncResult2.result().body()).isSuccess());
                        eventController.fire("nubeiot.edge.connector.bacnet.device.points-info",
                                             EventPattern.REQUEST_RESPONSE, EventMessage.initial(EventAction.GET_ONE,
                                                                                                 RequestData.builder()
                                                                                                            .body(
                                                                                                                addNetworkToJson(
                                                                                                                    new JsonObject())
                                                                                                                    .put(
                                                                                                                        "deviceId",
                                                                                                                        remoteDeviceId
                                                                                                                            .toString())
                                                                                                                    .put(
                                                                                                                        "objectId",
                                                                                                                        pointId))
                                                                                                            .build()),
                                             messageAsyncResult3 -> {
                                                 EventMessage message3 = EventMessage.tryParse(
                                                     messageAsyncResult3.result().body());
                                                 context.assertTrue(message3.isSuccess());
                                                 context.assertTrue(message3.getData().containsKey("priority-array"));
                                                 context.assertFalse(
                                                     message3.getData().getString("priority-array").contains("10=1"));
                                                 TestHelper.testComplete(async);
                                             }, null);
                    }, null);
            }, null);
    }

    @Test
    public void writePointNullTestAnalog(TestContext context) throws Exception {
        Async async = context.async();
        String pointId = BACnetDataConversions.pointIDNubeToBACnet("UO1");
        JsonObject point = testPoints.getJsonObject("UO1");
        double val = 0.12;
        RequestData req = RequestData.builder()
                                     .body(addNetworkToJson(new JsonObject()).put("deviceId", remoteDeviceId.toString())
                                                                             .put("objectId", pointId)
                                                                             .put("priority", "10")
                                                                             .put("value", val))
                                     .build();

        eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                             EventMessage.initial(EventAction.PATCH, req), messageAsyncResult -> {

                context.assertTrue(EventMessage.tryParse(messageAsyncResult.result().body()).isSuccess());
                req.body().remove("value");
                req.body().put("value", "null");

                eventController.fire("nubeiot.edge.connector.bacnet.device.points", EventPattern.REQUEST_RESPONSE,
                                     EventMessage.initial(EventAction.PATCH, req), messageAsyncResult2 -> {

                        context.assertTrue(EventMessage.tryParse(messageAsyncResult2.result().body()).isSuccess());
                        eventController.fire("nubeiot.edge.connector.bacnet.device.points-info",
                                             EventPattern.REQUEST_RESPONSE, EventMessage.initial(EventAction.GET_ONE,
                                                                                                 RequestData.builder()
                                                                                                            .body(
                                                                                                                addNetworkToJson(
                                                                                                                    new JsonObject())
                                                                                                                    .put(
                                                                                                                        "deviceId",
                                                                                                                        remoteDeviceId
                                                                                                                            .toString())
                                                                                                                    .put(
                                                                                                                        "objectId",
                                                                                                                        pointId))
                                                                                                            .build()),
                                             messageAsyncResult3 -> {
                                                 EventMessage message3 = EventMessage.tryParse(
                                                     messageAsyncResult3.result().body());
                                                 context.assertTrue(message3.isSuccess());
                                                 context.assertTrue(message3.getData().containsKey("priority-array"));
                                                 context.assertTrue(
                                                     message3.getData().getFloat("present-value") != val);
                                                 context.assertFalse(message3.getData()
                                                                             .getString("priority-array")
                                                                             .contains("10=" + Double.toString(val)));
                                                 TestHelper.testComplete(async);
                                             }, null);
                    }, null);
            }, null);
    }

    private JsonObject addNetworkToJson(JsonObject json) {
        return json.put("network", masterBACnetConfig.getNetworks().toNetworks().iterator().next().getName());
    }

}
