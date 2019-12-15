package com.nubeiot.core.micro;

import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.filter.RecordPredicate;

@RunWith(VertxUnitRunner.class)
public class ServiceGatewayIndexTest {

    private Vertx vertx;
    private MicroConfig config;
    private MicroContext micro;
    private EventController eventClient;

    @BeforeClass
    public static void init() {
        TestHelper.setup();
    }

    @Before
    @SuppressWarnings("unchecked")
    public void setUp(TestContext context) {
        Async async = context.async(3);
        config = IConfig.fromClasspath("local.json", MicroConfig.class);
        vertx = Vertx.vertx();
        micro = new MicroContext().setup(vertx, config);
        eventClient = SharedDataDelegate.getEventController(vertx, MicroContext.class.getName());
        final ServiceDiscoveryController discovery = micro.getLocalController();
        final Single<Record> record1 = discovery.addHttpRecord("http.test", new HttpLocation().setHost("123.456.0.1")
                                                                                              .setPort(1234)
                                                                                              .setRoot("/api"),
                                                               new JsonObject().put("meta", "test"));
        final Single<Record> record2 = discovery.addEventMessageRecord("event.message.1", "address.1",
                                                                       EventMethodDefinition.createDefault("/path",
                                                                                                           "/:param"));
        final Single<Record> record3 = discovery.addEventMessageRecord("event.message.2", "address.2",
                                                                       EventMethodDefinition.createDefault("/xy",
                                                                                                           "/:z"));
        Single.concatArray(record1, record2, record3)
              .subscribe(record -> TestHelper.testComplete(async), context::fail);
    }

    @After
    public void tearDown() {
        if (Objects.nonNull(micro)) {
            micro.unregister(Future.future());
        }
        vertx.close();
    }

    @Test
    public void test_get_by_name(TestContext context) {
        Async async = context.async();
        final JsonObject value = new JsonObject(
            "{\"endpoints\":[{\"method\":\"POST\",\"path\":\"/path\"},{\"method\":\"PUT\",\"path\":\"/path/:param\"}," +
            "{\"method\":\"DELETE\",\"path\":\"/path/:param\"},{\"method\":\"GET\",\"path\":\"/path/:param\"}," +
            "{\"method\":\"GET\",\"path\":\"/path\"},{\"method\":\"PATCH\",\"path\":\"/path/:param\"}]," +
            "\"name\":\"event.message.1\",\"location\":\"address.1\",\"status\":\"UP\"}");
        final JsonObject indexExpected = new JsonObject().put("status", Status.SUCCESS)
                                                         .put("action", EventAction.GET_ONE)
                                                         .put("data", value);
        final JsonObject payload = RequestData.builder()
                                              .body(new JsonObject().put(RecordPredicate.IDENTIFIER, "event.message.1"))
                                              .filter(new JsonObject().put(RecordPredicate.BY, "name"))
                                              .build()
                                              .toJson();
        eventClient.request(DeliveryEvent.builder()
                                         .address(config.getGatewayConfig().getIndexAddress())
                                         .payload(payload)
                                         .action(EventAction.GET_ONE)
                                         .build(),
                            EventbusHelper.replyAsserter(context, async, indexExpected, JSONCompareMode.LENIENT));
    }

    @Test
    public void test_get_by_group(TestContext context) {
        Async async = context.async();
        final JsonObject value = new JsonObject(
            "{\"name\":\"http.test\",\"location\":\"http://123.456.0.1:1234/api\",\"type\":\"http-endpoint\"," +
            "\"status\":\"UP\"}");
        final JsonObject indexExpected = new JsonObject().put("status", Status.SUCCESS)
                                                         .put("action", EventAction.GET_ONE)
                                                         .put("data", value);
        final JsonObject payload = RequestData.builder()
                                              .body(new JsonObject().put(RecordPredicate.IDENTIFIER, "http"))
                                              .filter(new JsonObject().put(RecordPredicate.BY, "group"))
                                              .build()
                                              .toJson();
        eventClient.request(DeliveryEvent.builder()
                                         .address(config.getGatewayConfig().getIndexAddress())
                                         .payload(payload)
                                         .action(EventAction.GET_ONE)
                                         .build(), EventbusHelper.replyAsserter(context, async, indexExpected));
    }

    @Test
    public void test_get_by_path(TestContext context) {
        Async async = context.async();
        final JsonObject expected = new JsonObject(
            "{\"status\":\"SUCCESS\",\"action\":\"GET_ONE\",\"data\":{\"endpoints\":[{\"method\":\"GET\"," +
            "\"path\":\"/xy\"},{\"method\":\"PATCH\",\"path\":\"/xy/:z\"},{\"method\":\"PUT\",\"path\":\"/xy/:z\"}," +
            "{\"method\":\"POST\",\"path\":\"/xy\"},{\"method\":\"DELETE\",\"path\":\"/xy/:z\"},{\"method\":\"GET\"," +
            "\"path\":\"/xy/:z\"}],\"name\":\"event.message.2\",\"location\":\"address.2\",\"status\":\"UP\"}}");
        final JsonObject payload = RequestData.builder()
                                              .body(new JsonObject().put(RecordPredicate.IDENTIFIER, "/xy"))
                                              .filter(new JsonObject().put(RecordPredicate.BY, "path"))
                                              .build()
                                              .toJson();
        eventClient.request(DeliveryEvent.builder()
                                         .address(config.getGatewayConfig().getIndexAddress())
                                         .payload(payload)
                                         .action(EventAction.GET_ONE).build(),
                            EventbusHelper.replyAsserter(context, async, expected, JSONCompareMode.LENIENT));
    }

    @Test
    public void test_list_by_group(TestContext context) {
        Async async = context.async();
        final JsonObject value = new JsonObject(
            "{\"apis\":[{\"name\":\"event.message.2\",\"status\":\"UP\",\"location\":\"address.2\"," +
            "\"endpoints\":[{\"method\":\"POST\",\"path\":\"/xy\"},{\"method\":\"PUT\",\"path\":\"/xy/:z\"}," +
            "{\"method\":\"DELETE\",\"path\":\"/xy/:z\"},{\"method\":\"GET\",\"path\":\"/xy/:z\"}," +
            "{\"method\":\"GET\",\"path\":\"/xy\"},{\"method\":\"PATCH\",\"path\":\"/xy/:z\"}]},{\"name\":\"http" +
            ".test\",\"type\":\"http-endpoint\",\"status\":\"UP\",\"location\":\"http://123.456.0.1:1234/api\"}," +
            "{\"name\":\"event.message.1\",\"status\":\"UP\",\"location\":\"address.1\"," +
            "\"endpoints\":[{\"method\":\"POST\",\"path\":\"/path\"},{\"method\":\"PUT\",\"path\":\"/path/:param\"}," +
            "{\"method\":\"DELETE\",\"path\":\"/path/:param\"},{\"method\":\"GET\",\"path\":\"/path/:param\"}," +
            "{\"method\":\"GET\",\"path\":\"/path\"},{\"method\":\"PATCH\",\"path\":\"/path/:param\"}]}]}");
        final JsonObject indexExpected = new JsonObject().put("status", Status.SUCCESS)
                                                         .put("action", EventAction.GET_LIST)
                                                         .put("data", value);
        final JsonObject payload = RequestData.builder()
                                              .body(new JsonObject().put(RecordPredicate.IDENTIFIER, "event.message"))
                                              .filter(new JsonObject().put(RecordPredicate.BY, "group"))
                                              .build()
                                              .toJson();
        eventClient.request(DeliveryEvent.builder()
                                         .address(config.getGatewayConfig().getIndexAddress())
                                         .payload(payload)
                                         .action(EventAction.GET_LIST)
                                         .build(),
                            EventbusHelper.replyAsserter(context, async, indexExpected, JSONCompareMode.LENIENT));
    }

}
