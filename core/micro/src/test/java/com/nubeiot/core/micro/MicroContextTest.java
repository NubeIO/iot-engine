package com.nubeiot.core.micro;

import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import com.nubeiot.core.micro.mock.MockEventbusService;

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
    public void tearDown(TestContext context) {
        if (Objects.nonNull(micro)) {
            micro.unregister(Future.future());
        }
        vertx.close();
    }

    @Test(expected = NullPointerException.class)
    public void test_not_enable_serviceDiscovery_cluster() {
        new MicroContext().create(vertx, IConfig.fromClasspath("micro.json", MicroConfig.class))
                          .getClusterController()
                          .get();
    }

    @Test(expected = NullPointerException.class)
    public void test_not_enable_serviceDiscovery_local() {
        new MicroContext().create(vertx, IConfig.fromClasspath("micro.json", MicroConfig.class))
                          .getLocalController()
                          .get();
    }

    @Test(expected = NullPointerException.class)
    public void test_not_enable_circuitBreaker() {
        new MicroContext().create(vertx, IConfig.fromClasspath("micro.json", MicroConfig.class))
                          .getBreakerController()
                          .get();
    }

    @Test
    public void test_enable_serviceDiscovery_local_and_circuitBreaker() {
        MicroContext context = new MicroContext().create(vertx, IConfig.fromClasspath("local.json", MicroConfig.class));
        context.getLocalController().get();
        context.getBreakerController().get();
    }

    @Test
    public void test_serviceDiscovery_local_register_eventbus(TestContext context) {
        Async async = context.async();
        micro = new MicroContext().create(vertx, IConfig.fromClasspath("local.json", MicroConfig.class));
        JsonObject expected = new JsonObject("{\"location\":{\"endpoint\":\"address1\"},\"metadata\":{\"service" +
                                             ".interface\":\"com.nubeiot.core.micro.mock.MockEventbusService\"}," +
                                             "\"name\":\"test\",\"status\":\"UP\"," +
                                             "\"type\":\"eventbus-service-proxy\"}");
        EventbusHelper.assertReceivedData(vertx, async, micro.getLocalController().getConfig().getAnnounceAddress(),
                                          JsonHelper.asserter(context, async, expected));
        micro.getLocalController()
             .addRecord(EventBusService.createRecord("test", "address1", MockEventbusService.class))
             .subscribe();
    }

    @Test
    public void test_serviceDiscovery_local_register_http(TestContext context) {
        Async async = context.async();
        micro = new MicroContext().create(vertx, IConfig.fromClasspath("local.json", MicroConfig.class));
        JsonObject expected = new JsonObject("{\"location\":{\"endpoint\":\"http://123.456.0.1:1234/api\"," +
                                             "\"host\":\"123.456.0.1\",\"port\":1234,\"root\":\"/api\"," +
                                             "\"ssl\":false},\"metadata\":{\"meta\":\"test\"},\"name\":\"http.test\"," +
                                             "\"status\":\"UP\",\"type\":\"http-endpoint\"}");
        EventbusHelper.assertReceivedData(vertx, async, micro.getLocalController().getConfig().getAnnounceAddress(),
                                          JsonHelper.asserter(context, async, expected));
        micro.getLocalController()
             .addHttpRecord("http.test", new HttpLocation().setHost("123.456.0.1").setPort(1234).setRoot("/api"),
                            new JsonObject().put("meta", "test"))
             .subscribe();
    }

}
