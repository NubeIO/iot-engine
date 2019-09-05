package com.nubeiot.core.micro;

import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.mock.MockEventbusService;
import com.nubeiot.core.micro.type.EventMessageService;

@RunWith(VertxUnitRunner.class)
public class MicroContextTest {

    private Vertx vertx;
    private MicroContext micro;

    @BeforeClass
    public static void init() {
        TestHelper.setup();
    }

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        if (Objects.nonNull(micro)) {
            micro.unregister(Future.future());
        }
        vertx.close();
    }

    @Test(expected = NullPointerException.class)
    public void test_not_enable_serviceDiscovery_cluster() {
        new MicroContext().setup(vertx, IConfig.fromClasspath("micro.json", MicroConfig.class))
                          .getClusterController()
                          .get();
    }

    @Test(expected = NullPointerException.class)
    public void test_not_enable_serviceDiscovery_local() {
        new MicroContext().setup(vertx, IConfig.fromClasspath("micro.json", MicroConfig.class))
                          .getLocalController()
                          .get();
    }

    @Test(expected = NullPointerException.class)
    public void test_not_enable_circuitBreaker() {
        new MicroContext().setup(vertx, IConfig.fromClasspath("micro.json", MicroConfig.class))
                          .getBreakerController()
                          .get();
    }

    @Test
    public void test_enable_serviceDiscovery_local_and_circuitBreaker() {
        MicroContext context = new MicroContext().setup(vertx, IConfig.fromClasspath("local.json", MicroConfig.class));
        context.getLocalController().get();
        context.getBreakerController().get();
    }

    @Test
    public void test_serviceDiscovery_local_register_eventbus(TestContext context) {
        final Async async = context.async(2);
        final MicroConfig config = IConfig.fromClasspath("local.json", MicroConfig.class);
        micro = new MicroContext().setup(vertx, IConfig.fromClasspath("local.json", MicroConfig.class));
        JsonObject expected = new JsonObject("{\"location\":{\"endpoint\":\"address1\"},\"metadata\":{\"service" +
                                             ".interface\":\"com.nubeiot.core.micro.mock.MockEventbusService\"}," +
                                             "\"name\":\"test\",\"status\":\"UP\"," +
                                             "\"type\":\"eventbus-service-proxy\"}");
        EventbusHelper.assertReceivedData(vertx, async, micro.getLocalController().getConfig().getAnnounceAddress(),
                                          JsonHelper.asserter(context, async, expected));
        EventController controller = SharedDataDelegate.getEventController(vertx, MicroContext.class.getName());
        micro.getLocalController()
             .addRecord(EventBusService.createRecord("test", "address1", MockEventbusService.class))
             .subscribe(record -> {
                 final JsonObject indexExpected = new JsonObject(
                     "{\"status\":\"SUCCESS\",\"action\":\"GET_LIST\",\"data\":{\"records\":[{\"name\":\"test\"," +
                     "\"type\":\"eventbus-service-proxy\",\"status\":\"UP\"," +
                     "\"location\":{\"endpoint\":\"address1\"}}]}}");
                 final JsonObject payload = RequestData.builder()
                                                       .filter(new JsonObject().put("scope", ServiceScope.INTERNAL))
                                                       .build()
                                                       .toJson();
                 controller.request(DeliveryEvent.builder()
                                                 .address(config.getGatewayConfig().getIndexAddress())
                                                 .payload(payload)
                                                 .action(EventAction.GET_LIST)
                                                 .build(), EventbusHelper.replyAsserter(context, async, indexExpected));
             });
    }

    @Test
    public void test_serviceDiscovery_local_register_http(TestContext context) {
        final Async async = context.async(2);
        final MicroConfig config = IConfig.fromClasspath("local.json", MicroConfig.class);
        micro = new MicroContext().setup(vertx, config);
        JsonObject expected = new JsonObject("{\"location\":{\"endpoint\":\"http://123.456.0.1:1234/api\"," +
                                             "\"host\":\"123.456.0.1\",\"port\":1234,\"root\":\"/api\"," +
                                             "\"ssl\":false},\"metadata\":{\"meta\":\"test\"},\"name\":\"http.test\"," +
                                             "\"status\":\"UP\",\"type\":\"http-endpoint\"}");
        EventbusHelper.assertReceivedData(vertx, async, micro.getLocalController().getConfig().getAnnounceAddress(),
                                          JsonHelper.asserter(context, async, expected));
        EventController controller = SharedDataDelegate.getEventController(vertx, MicroContext.class.getName());
        micro.getLocalController()
             .addHttpRecord("http.test", new HttpLocation().setHost("123.456.0.1").setPort(1234).setRoot("/api"),
                            new JsonObject().put("meta", "test")).subscribe(record -> {
            final JsonObject indexExpected = new JsonObject(
                "{\"status\":\"SUCCESS\",\"action\":\"GET_LIST\",\"data\":{\"records\":[{\"name\":\"http.test\"," +
                "\"status\":\"UP\",\"type\":\"http-endpoint\",\"location\":\"http://123.456.0.1:1234/api\"}]}}");
            controller.request(DeliveryEvent.builder()
                                            .address(config.getGatewayConfig().getIndexAddress())
                                            .payload(RequestData.builder().build().toJson())
                                            .action(EventAction.GET_LIST)
                                            .build(), EventbusHelper.replyAsserter(context, async, indexExpected));
        });
    }

    @Test
    public void test_serviceDiscovery_local_register_eventMessage(TestContext context) {
        final Async async = context.async(2);
        final MicroConfig config = IConfig.fromClasspath("local.json", MicroConfig.class);
        micro = new MicroContext().setup(vertx, config);
        JsonObject expected = new JsonObject(
            "{\"location\":{\"endpoint\":\"address.1\"},\"metadata\":{\"eventMethods\":{\"servicePath\":\"/path\"," +
            "\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\",\"capturePath\":\"/path\"," +
            "\"regexPath\":\"/path\"},{\"action\":\"UPDATE\",\"method\":\"PUT\",\"capturePath\":\"/path/:param\"," +
            "\"regexPath\":\"/path/.+\"},{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath\":\"/path\"," +
            "\"regexPath\":\"/path\"},{\"action\":\"PATCH\",\"method\":\"PATCH\",\"capturePath\":\"/path/:param\"," +
            "\"regexPath\":\"/path/.+\"},{\"action\":\"GET_ONE\",\"method\":\"GET\",\"capturePath\":\"/path/:param\"," +
            "\"regexPath\":\"/path/.+\"},{\"action\":\"REMOVE\",\"method\":\"DELETE\"," +
            "\"capturePath\":\"/path/:param\",\"regexPath\":\"/path/.+\"}],\"useRequestData\":true}}," +
            "\"name\":\"event-message\",\"status\":\"UP\",\"type\":\"eventbus-message-service\"}");
        EventbusHelper.assertReceivedData(vertx, async, micro.getLocalController().getConfig().getAnnounceAddress(),
                                          JsonHelper.asserter(context, async, expected, JSONCompareMode.LENIENT));
        EventController controller = SharedDataDelegate.getEventController(vertx, MicroContext.class.getName());
        micro.getLocalController()
             .addRecord(EventMessageService.createRecord("event-message", "address.1",
                                                         EventMethodDefinition.createDefault("/path", "/:param")))
             .subscribe(record -> {
                 final JsonObject value = new JsonObject(
                     "{\"records\":[{\"name\":\"event-message\",\"status\":\"UP\",\"location\":[{\"method\":\"GET\"," +
                     "\"path\":\"/path/:param\"},{\"method\":\"PATCH\",\"path\":\"/path/:param\"}," +
                     "{\"method\":\"GET\",\"path\":\"/path\"},{\"method\":\"POST\",\"path\":\"/path\"}," +
                     "{\"method\":\"DELETE\",\"path\":\"/path/:param\"},{\"method\":\"PUT\"," +
                     "\"path\":\"/path/:param\"}]}]}");
                 final JsonObject indexExpected = new JsonObject().put("status", Status.SUCCESS)
                                                                  .put("action", EventAction.GET_LIST)
                                                                  .put("data", value);
                 controller.request(DeliveryEvent.builder()
                                                 .address(config.getGatewayConfig().getIndexAddress())
                                                 .payload(RequestData.builder().build().toJson())
                                                 .action(EventAction.GET_LIST)
                                                 .build(), EventbusHelper.replyAsserter(context, async, indexExpected));
             });
    }

}
