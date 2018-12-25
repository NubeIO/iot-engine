package com.nubeiot.core.http.ws;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.BaseHttpServerTest;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.mock.MockWebsocketEvent;
import com.nubeiot.core.http.utils.Urls;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.http.WebsocketRejectedException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class WebsocketEventServerTest extends BaseHttpServerTest {

    @Rule
    public Timeout timeoutRule = Timeout.seconds(BaseHttpServerTest.DEFAULT_TIMEOUT);

    @BeforeClass
    public static void beforeSuite() {
        BaseHttpServerTest.beforeSuite();
    }

    private RequestOptions requestOptions;

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        this.httpConfig.put("enabled", false).put("__socket__", new JsonObject().put("enabled", true));
        this.requestOptions = new RequestOptions().setHost(DEFAULT_HOST).setPort(httpConfig.getInteger("port"));
        new MockWebsocketEvent.MockWebsocketEventServerHandler(vertx.eventBus()).start();
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Test(expected = InitializerError.class)
    public void test_not_register() {
        startServer(new HttpServerRouter());
    }

    @Test
    public void test_greeting(TestContext context) {
        startServer(new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.ALL_EVENTS));
        Async async = context.async(2);
        assertGreeting(context, async, "/ws/");
        assertGreeting(context, async, "/ws");
    }

    @Test
    public void test_not_found(TestContext context) {
        Async async = context.async(3);
        startServer(new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.ALL_EVENTS));
        assertNotFound(context, async, "/socket1");
        assertNotFound(context, async, "/ws//");
        assertNotFound(context, async, "/ws/xyz");
    }

    @Test
    public void test_send_with_publisher(TestContext context) throws InterruptedException {
        JsonObject consumerExpected = EventMessage.success(EventAction.GET_LIST,
                                                           new JsonObject().put("data", Arrays.asList("1", "2", "3")))
                                                  .toJson();
        JsonObject responseExpected = EventMessage.success(EventAction.RETURN).toJson();
        String pushAddress = MockWebsocketEvent.ALL_EVENTS.getListener().getAddress();
        EventMessage message = EventMessage.success(EventAction.GET_LIST, new JsonObject().put("echo", 1));
        startServer(new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.ALL_EVENTS));
        Async async = context.async(2);
        setupConsumer(async, MockWebsocketEvent.ALL_EVENTS.getPublisher().getAddress(),
                      o -> assertResponse(context, async, consumerExpected, Buffer.buffer(((JsonObject) o).encode())));
        WebSocket ws = setupSockJsClient(async, context::fail);
        clientSend(pushAddress, message).accept(
                ws.handler(buffer -> assertResponse(context, async, responseExpected, buffer)));
    }

    @Test
    public void test_send_with_no_publisher(TestContext context) throws InterruptedException {
        JsonObject responseExpected = new JsonObject(
                "{\"status\":\"SUCCESS\",\"action\":\"GET_ONE\",\"data\":{\"data\":\"1\"}}");
        String pushAddress = MockWebsocketEvent.NO_PUBLISHER.getListener().getAddress();
        EventMessage message = EventMessage.success(EventAction.GET_ONE, new JsonObject().put("echo", 1));
        startServer(new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.NO_PUBLISHER));
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(async, context::fail);
        clientSend(pushAddress, message).accept(
                ws.handler(buffer -> assertResponse(context, async, responseExpected, buffer)));
    }

    @Test
    public void test_client_listen_only_publisher(TestContext context) {
        EventMessage echo = EventMessage.success(EventAction.GET_ONE, new JsonObject().put("echo", 1));
        EventModel publisher = MockWebsocketEvent.ONLY_PUBLISHER.getPublisher();
        EventController controller = new EventController(vertx);
        vertx.setPeriodic(1000, t -> controller.response(publisher.getAddress(), publisher.getPattern(), echo));
        Async async = context.async(1);
        setupConsumer(async, publisher.getAddress(),
                      o -> assertResponse(context, async, echo.toJson(), (JsonObject) o));
    }

    @Test
    public void test_web_listen_only_publisher(TestContext context) throws InterruptedException {
        EventModel publisher = MockWebsocketEvent.ONLY_PUBLISHER.getPublisher();
        EventMessage echo = EventMessage.success(EventAction.GET_ONE, new JsonObject().put("echo", 1));
        JsonObject expected = createWebsocketMsg(publisher.getAddress(), echo, BridgeEventType.RECEIVE);
        EventController controller = new EventController(vertx);
        String path = Urls.combinePath("/ws", MockWebsocketEvent.ONLY_PUBLISHER.getPath());
        startServer(new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.ONLY_PUBLISHER));
        vertx.setPeriodic(1000, t -> controller.response(publisher.getAddress(), publisher.getPattern(), echo));
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(async, path, clientRegister(publisher.getAddress()), context::fail);
        ws.handler(buffer -> assertResponse(context, async, expected, buffer));
    }

    @Test
    public void test_send_error_json_format(TestContext context) throws InterruptedException {
        JsonObject expected = new JsonObject("{\"type\":\"err\",\"body\":\"invalid_json\"}");
        startServer(new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.NO_PUBLISHER));
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(async, context::fail);
        ws.handler(buffer -> assertResponse(context, async, expected, buffer));
        ws.writeTextMessage("xx");
    }

    @Test
    public void test_send_error_websocketMessage_format(TestContext context) throws InterruptedException {
        EventMessage body = EventMessage.error(EventAction.RETURN, NubeException.ErrorCode.INVALID_ARGUMENT,
                                               "Invalid websocket event body format");
        startServer(new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.NO_PUBLISHER));
        String pushAddress = MockWebsocketEvent.ALL_EVENTS.getListener().getAddress();
        JsonObject socketMsg = createWebsocketMsg(pushAddress, null, BridgeEventType.SEND);
        JsonObject msg = socketMsg.put("body", new JsonObject("{\"type\":\"err\"}"));
        Async async = context.async(1);
        WebSocket ws = setupSockJsClient(async, context::fail);
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

    private void assertResponse(TestContext context, Async async, JsonObject expected, Buffer actual) {
        assertResponse(context, async, expected, actual.toJsonObject());
    }

    private void assertResponse(TestContext context, Async async, JsonObject expected, JsonObject actual) {
        try {
            JSONAssert.assertEquals(expected.encode(), actual.encode(), JSONCompareMode.STRICT);
        } catch (JSONException e) {
            context.fail(e);
        } finally {
            testComplete(async);
        }
    }

    private void setupConsumer(Async async, String address, Consumer<Object> assertOut) {
        MessageConsumer<Object> consumer = vertx.getDelegate().eventBus().consumer(address);
        consumer.handler(event -> {
            System.out.println("Received message from address: " + address);
            assertOut.accept(event.body());
            consumer.unregister(v -> testComplete(async, "CONSUMER END"));
        });
    }

    private WebSocket setupSockJsClient(Async async, Consumer<Throwable> error) throws InterruptedException {
        return setupSockJsClient(async, "/ws", null, error);
    }

    private WebSocket setupSockJsClient(Async async, String path, Consumer<WebSocket> writerBeforeHandler,
                                        Consumer<Throwable> error) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<WebSocket> wsReference = new AtomicReference<>();
        client.websocket(requestOptions.setURI(Urls.combinePath(path, "/websocket")), ws -> {
            if (Objects.nonNull(writerBeforeHandler)) {
                writerBeforeHandler.accept(ws.getDelegate());
            }
            wsReference.set(ws.getDelegate());
            countDownLatch.countDown();
            ws.endHandler(v -> testComplete(async, "CLIENT END"));
            ws.exceptionHandler(error::accept);
        }, error::accept);
        countDownLatch.await(1200, TimeUnit.MILLISECONDS);
        return wsReference.get();
    }

    private Consumer<WebSocket> clientRegister(String address) {
        return clientWrite(createWebsocketMsg(address, null, BridgeEventType.REGISTER));
    }

    private Consumer<WebSocket> clientSend(String address, EventMessage body) {
        return clientWrite(createWebsocketMsg(address, body, BridgeEventType.SEND));
    }

    private Consumer<WebSocket> clientWrite(JsonObject data) {
        return ws -> ws.writeFrame(WebSocketFrame.textFrame(data.encode(), true));
    }

    private JsonObject createWebsocketMsg(String address, EventMessage body, BridgeEventType send) {
        return WebsocketEventMessage.builder().type(send).address(address).body(body).build().toJson();
    }

}
