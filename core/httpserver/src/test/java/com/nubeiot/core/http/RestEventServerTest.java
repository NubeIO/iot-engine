package com.nubeiot.core.http;

import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.mock.MockApiDefinition;
import com.nubeiot.core.http.mock.MockEventBusErrorHandler;
import com.nubeiot.core.http.mock.MockEventBusSuccessHandler;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@SuppressWarnings("unchecked")
@RunWith(VertxUnitRunner.class)
public class RestEventServerTest extends BaseHttpServerTest {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();
    @Rule
    public Timeout timeoutRule = Timeout.seconds(BaseHttpServerTest.DEFAULT_TIMEOUT);

    @BeforeClass
    public static void beforeSuite() {
        BaseHttpServerTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Test
    public void test_api_eventbus_not_found(TestContext context) {
        int port = httpConfig.getInteger("port");
        String path = "/api/test/event";
        JsonObject expected = notFoundResponse(port, path);
        startServer(new HttpServerRouter().registerEventBusApi(MockApiDefinition.MockRestEventApi.class));
        assertRestByClient(context, HttpMethod.GET, port, path, 404, expected);
    }

    @Test
    public void test_api_eventbus_error_unexpected(TestContext context) {
        MockEventBusErrorHandler.create(this.vertx.eventBus()).start();
        int port = httpConfig.getInteger("port");
        String path = "/api/test/events";
        JsonObject expected = new JsonObject().put("code", NubeException.ErrorCode.UNKNOWN_ERROR)
                                              .put("message", "UNKNOWN_ERROR | Cause: xxx");
        startServer(new HttpServerRouter().registerEventBusApi(MockApiDefinition.MockRestEventApi.class));
        assertRestByClient(context, HttpMethod.GET, port, path, 500, expected);
    }

    @Test
    public void test_api_eventbus_error_from_server(TestContext context) {
        MockEventBusErrorHandler.create(this.vertx.eventBus()).start();
        int port = httpConfig.getInteger("port");
        String path = "/api/test/events";
        JsonObject expected = new JsonObject().put("code", NubeException.ErrorCode.ENGINE_ERROR)
                                              .put("message", "Engine error");
        startServer(new HttpServerRouter().registerEventBusApi(MockApiDefinition.MockRestEventApi.class));
        assertRestByClient(context, HttpMethod.POST, port, path, 500, expected);
    }

    @Test
    public void test_api_eventbus_error_from_user(TestContext context) {
        MockEventBusErrorHandler.create(this.vertx.eventBus()).start();
        int port = httpConfig.getInteger("port");
        String path = "/api/test/event/:event_id";
        JsonObject expected = new JsonObject().put("code", NubeException.ErrorCode.INVALID_ARGUMENT)
                                              .put("message", "invalid");
        startServer(new HttpServerRouter().registerEventBusApi(MockApiDefinition.MockRestEventApi.class));
        assertRestByClient(context, HttpMethod.PUT, port, path, 400, expected);
    }

    @Test
    public void test_api_eventbus_no_reply(TestContext context) {
        int port = httpConfig.getInteger("port");
        String path = "/api/test/event/:event_id";
        JsonObject expected = new JsonObject().put("code", NubeException.ErrorCode.SERVICE_ERROR)
                                              .put("message", "Service unavailable");
        startServer(new HttpServerRouter().registerEventBusApi(MockApiDefinition.MockRestEventApi.class));
        assertRestByClient(context, HttpMethod.GET, port, path, 503, expected);
    }

    @Test
    public void test_api_eventbus_success_data_list(TestContext context) {
        MockEventBusSuccessHandler.create(this.vertx.eventBus()).start();
        int port = httpConfig.getInteger("port");
        String path = "/api/test/events";
        JsonObject expected = new JsonObject().put("data", Arrays.asList("1", "2", "3"));
        startServer(new HttpServerRouter().registerEventBusApi(MockApiDefinition.MockRestEventApi.class));
        assertRestByClient(context, HttpMethod.GET, port, path, 200, expected);
    }

    @Test
    public void test_api_eventbus_success_data_other(TestContext context) {
        MockEventBusSuccessHandler.create(this.vertx.eventBus()).start();
        int port = httpConfig.getInteger("port");
        String path = "/api/test/event/1";
        JsonObject expected = new JsonObject().put("data", 1);
        startServer(new HttpServerRouter().registerEventBusApi(MockApiDefinition.MockRestEventApi.class));
        assertRestByClient(context, HttpMethod.GET, port, path, 200, expected);
    }

    @Test
    public void test_api_eventbus_success_data_json(TestContext context) {
        MockEventBusSuccessHandler.create(this.vertx.eventBus()).start();
        int port = httpConfig.getInteger("port");
        String path = "/api/test/events";
        JsonObject expected = new JsonObject().put("create", "success");
        startServer(new HttpServerRouter().registerEventBusApi(MockApiDefinition.MockRestEventApi.class));
        assertRestByClient(context, HttpMethod.POST, port, path, 201, expected);
    }

    @Test
    public void test_api_eventbus_success_data_single(TestContext context) {
        MockEventBusSuccessHandler.create(this.vertx.eventBus()).start();
        int port = httpConfig.getInteger("port");
        String path = "/api/test/event/1";
        JsonObject expected = new JsonObject().put("data", "success");
        startServer(new HttpServerRouter().registerEventBusApi(MockApiDefinition.MockRestEventApi.class));
        assertRestByClient(context, HttpMethod.PUT, port, path, 200, expected);
    }

    @Test
    public void test_api_eventbus_success_data_single_json(TestContext context) {
        MockEventBusSuccessHandler.create(this.vertx.eventBus()).start();
        int port = httpConfig.getInteger("port");
        String path = "/api/test/event/1";
        JsonObject expected = new JsonObject().put("patch", "success").put("event_id", 1);
        startServer(new HttpServerRouter().registerEventBusApi(MockApiDefinition.MockRestEventApi.class));
        assertRestByClient(context, HttpMethod.PATCH, port, path, 200, expected);
    }

}
