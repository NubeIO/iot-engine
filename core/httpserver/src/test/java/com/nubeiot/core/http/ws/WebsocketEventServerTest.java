package com.nubeiot.core.http.ws;

import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebsocketRejectedException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.HttpServerTestBase;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.mock.MockWebsocketEvent;
import com.nubeiot.core.http.mock.MockWebsocketEvent.MockWebsocketEventServerHandler;

@RunWith(VertxUnitRunner.class)
public class WebsocketEventServerTest extends HttpServerTestBase {

    @Rule
    public Timeout timeout = Timeout.seconds(TestHelper.TEST_TIMEOUT_SEC);

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        this.enableWebsocket();
        try {
            new MockWebsocketEventServerHandler(vertx.eventBus()).start();
        } catch (Exception e) {
            context.fail(e);
        }
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Test
    public void test_not_register(TestContext context) {
        startServer(context, new HttpServerRouter(), t -> context.assertTrue(t instanceof InitializerError));
    }

    @Test
    public void test_greeting(TestContext context) {
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.ALL_EVENTS));
        Async async = context.async(2);
        assertGreeting(context, async, "/ws/");
        assertGreeting(context, async, "/ws");
    }

    @Test
    public void test_not_found(TestContext context) {
        Async async = context.async(3);
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.ALL_EVENTS));
        assertNotFound(context, async, "/socket1");
        assertNotFound(context, async, "/ws//");
        assertNotFound(context, async, "/ws/xyz");
    }

    @Test
    public void test_send_with_publisher(TestContext context) throws InterruptedException {
        JsonObject expected = EventMessage.success(EventAction.GET_LIST,
                                                   new JsonObject().put("data", Arrays.asList("1", "2", "3"))).toJson();
        JsonObject responseExpected = EventMessage.success(EventAction.RETURN).toJson();
        String pushAddress = MockWebsocketEvent.ALL_EVENTS.getListener().getAddress();
        EventMessage message = EventMessage.success(EventAction.GET_LIST, new JsonObject().put("echo", 1));
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.ALL_EVENTS));
        Async async = context.async(2);
        assertConsumerData(async, MockWebsocketEvent.ALL_EVENTS.getPublisher().getAddress(),
                           o -> JsonHelper.assertJson(context, async, expected, (JsonObject) o));
        WebSocket ws = setupSockJsClient(context, async, context::fail);
        clientSend(pushAddress, message).accept(
            ws.handler(buffer -> assertResponse(context, async, responseExpected, buffer)));
    }

    @Test
    public void test_send_with_no_publisher(TestContext context) throws InterruptedException {
        JsonObject responseExpected = new JsonObject(
            "{\"status\":\"SUCCESS\",\"action\":\"GET_ONE\",\"data\":{\"data\":\"1\"}}");
        String pushAddress = MockWebsocketEvent.NO_PUBLISHER.getListener().getAddress();
        EventMessage message = EventMessage.success(EventAction.GET_ONE, new JsonObject().put("echo", 1));
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.NO_PUBLISHER));
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(context, async, context::fail);
        clientSend(pushAddress, message).accept(
            ws.handler(buffer -> assertResponse(context, async, responseExpected, buffer)));
    }

    @Test
    public void test_client_listen_only_publisher(TestContext context) {
        EventMessage echo = EventMessage.success(EventAction.GET_ONE, new JsonObject().put("echo", 1));
        EventModel publisher = MockWebsocketEvent.ONLY_PUBLISHER.getPublisher();
        EventController controller = EventController.getInstance(vertx);
        vertx.setPeriodic(1000, t -> controller.response(publisher.getAddress(), publisher.getPattern(), echo));
        Async async = context.async(1);
        assertConsumerData(async, publisher.getAddress(),
                           o -> JsonHelper.assertJson(context, async, echo.toJson(), (JsonObject) o));
    }

    @Test
    public void test_web_listen_only_publisher(TestContext context) throws InterruptedException {
        EventModel publisher = MockWebsocketEvent.ONLY_PUBLISHER.getPublisher();
        EventMessage echo = EventMessage.success(EventAction.GET_ONE, new JsonObject().put("echo", 1));
        JsonObject expected = createWebsocketMsg(publisher.getAddress(), echo, BridgeEventType.RECEIVE);
        EventController controller = EventController.getInstance(vertx);
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.ONLY_PUBLISHER));
        vertx.setPeriodic(1000, t -> controller.response(publisher.getAddress(), publisher.getPattern(), echo));
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(context, async,
                                         Urls.combinePath("/ws", MockWebsocketEvent.ONLY_PUBLISHER.getPath()),
                                         clientRegister(publisher.getAddress()), context::fail);
        ws.handler(buffer -> assertResponse(context, async, expected, buffer));
    }

    @Test
    public void test_send_error_json_format(TestContext context) throws InterruptedException {
        JsonObject expected = new JsonObject("{\"type\":\"err\",\"body\":\"invalid_json\"}");
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.NO_PUBLISHER));
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(context, async, context::fail);
        ws.handler(buffer -> assertResponse(context, async, expected, buffer));
        ws.writeTextMessage("xx");
    }

    @Test
    public void test_send_error_websocketMessage_format(TestContext context) throws InterruptedException {
        EventMessage body = EventMessage.error(EventAction.RETURN, NubeException.ErrorCode.INVALID_ARGUMENT,
                                               "Invalid websocket event body format");
        startServer(context, new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.NO_PUBLISHER));
        String pushAddress = MockWebsocketEvent.ALL_EVENTS.getListener().getAddress();
        JsonObject socketMsg = createWebsocketMsg(pushAddress, null, BridgeEventType.SEND);
        JsonObject msg = socketMsg.put("body", new JsonObject("{\"type\":\"err\"}"));
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(context, async, context::fail);
        clientWrite(msg).accept(ws.handler(buffer -> assertResponse(context, async, body.toJson(), buffer)));
    }

    private void assertGreeting(TestContext context, Async async, String uri) {
        client.getNow(requestOptions.setURI(uri), resp -> {
            context.assertEquals(200, resp.statusCode());
            context.assertEquals("text/plain; charset=UTF-8", resp.getHeader("content-type"));
            resp.bodyHandler(buff -> {
                context.assertEquals("Welcome to SockJS!\n", buff.toString());
                testComplete(async);
            });
        });
    }

    private void assertNotFound(TestContext context, Async async, String uri) {
        client.websocket(requestOptions.setURI(uri), websocket -> testComplete(async), t -> {
            try {
                if (t instanceof WebsocketRejectedException) {
                    context.assertEquals(404, ((WebsocketRejectedException) t).getStatus());
                }
            } finally {
                testComplete(async);
            }
        });
    }

}
