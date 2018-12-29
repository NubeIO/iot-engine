package com.nubeiot.core.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.LoggerFactory;

import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.http.utils.Urls;
import com.nubeiot.core.http.ws.WebsocketEventMessage;
import com.nubeiot.core.utils.Strings;
import com.zandero.rest.RestRouter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpClient;

public class BaseHttpServerTest {

    private static final String DEFAULT_HOST = "127.0.0.1";
    protected static final int DEFAULT_TIMEOUT = 5000;
    protected Vertx vertx;
    protected JsonObject httpConfig;
    protected HttpClient client;
    protected RequestOptions requestOptions;
    private HttpServer httpServer;

    @BeforeClass
    protected static void beforeSuite() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
    }

    protected void before(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        httpConfig = new JsonObject().put("port", getRandomPort()).put("host", DEFAULT_HOST);
        client = vertx.createHttpClient(createClientOptions());
        requestOptions = new RequestOptions().setHost(DEFAULT_HOST).setPort(httpConfig.getInteger("port"));
    }

    protected void after(TestContext context) {
        RestRouter.getWriters().clear();
        RestRouter.getReaders().clear();
        RestRouter.getContextProviders().clear();
        RestRouter.getExceptionHandlers().clear();
        if (Objects.nonNull(httpServer)) {
            httpServer.stop();
        }
        vertx.close(context.asyncAssertSuccess());
    }

    private int getRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    protected HttpClientOptions createClientOptions() {
        return new HttpClientOptions().setConnectTimeout(DEFAULT_TIMEOUT);
    }

    protected void assertRestByClient(TestContext context, HttpMethod method, int port, String path, int codeExpected,
                                      JsonObject bodyExpected) {
        Async async = context.async();
        client.request(method, requestOptions.setURI(path), resp -> {
            context.assertEquals(ApiConstants.DEFAULT_CONTENT_TYPE, resp.getHeader(ApiConstants.CONTENT_TYPE));
            //            context.assertNotNull(resp.getHeader("x-response-time"));
            context.assertEquals(codeExpected, resp.statusCode());
            resp.bodyHandler(body -> assertResponseBody(context, bodyExpected, body));
        }).endHandler(event -> testComplete(async)).end();
    }

    protected void startServer(HttpServerRouter httpRouter) {
        httpServer = new HttpServer(vertx.getDelegate(), httpConfig.mapTo(HttpConfig.class), httpRouter);
        httpServer.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void assertResponseBody(TestContext context, JsonObject bodyExpected, Buffer body) {
        try {
            JSONAssert.assertEquals(bodyExpected.encode(), body.toJsonObject().encode(), JSONCompareMode.STRICT);
        } catch (JSONException | AssertionError e) {
            context.fail(e);
        }
    }

    protected JsonObject notFoundResponse(int port, String path) {
        return new JsonObject().put("message", "Resource not found")
                               .put("uri", Strings.format("http://{0}:{1}{2}", DEFAULT_HOST, port, path));
    }

    protected void testComplete(Async async) {
        this.testComplete(async, "");
    }

    protected void testComplete(Async async, String msgEvent) {
        System.out.println("Count:" + async.count());
        System.out.println(msgEvent);
        if (async.count() > 0) {
            async.countDown();
        }
        if (async.count() == 0 && !async.isCompleted()) {
            async.complete();
            client.close();
        }
    }

    protected void assertResponse(TestContext context, Async async, JsonObject expected,
                                  io.vertx.core.buffer.Buffer actual) {
        assertResponse(context, async, expected, actual.toJsonObject());
    }

    protected void assertResponse(TestContext context, Async async, JsonObject expected, JsonObject actual) {
        try {
            JSONAssert.assertEquals(expected.encode(), actual.encode(), JSONCompareMode.STRICT);
        } catch (JSONException | AssertionError e) {
            context.fail(e);
        } finally {
            testComplete(async);
        }
    }

    protected void setupConsumer(Async async, String address, Consumer<Object> assertOut) {
        MessageConsumer<Object> consumer = vertx.getDelegate().eventBus().consumer(address);
        consumer.handler(event -> {
            System.out.println("Received message from address: " + address);
            assertOut.accept(event.body());
            consumer.unregister(v -> testComplete(async, "CONSUMER END"));
        });
    }

    protected WebSocket setupSockJsClient(Async async, Consumer<Throwable> error) throws InterruptedException {
        return setupSockJsClient(async, "/ws", null, error);
    }

    protected WebSocket setupSockJsClient(Async async, String path, Consumer<WebSocket> writerBeforeHandler,
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
        countDownLatch.await(3000, TimeUnit.MILLISECONDS);
        return wsReference.get();
    }

    protected Consumer<WebSocket> clientRegister(String address) {
        return clientWrite(createWebsocketMsg(address, null, BridgeEventType.REGISTER));
    }

    protected Consumer<WebSocket> clientSend(String address, EventMessage body) {
        return clientWrite(createWebsocketMsg(address, body, BridgeEventType.SEND));
    }

    protected Consumer<WebSocket> clientWrite(JsonObject data) {
        return ws -> ws.writeFrame(WebSocketFrame.textFrame(data.encode(), true));
    }

    protected JsonObject createWebsocketMsg(String address, EventMessage body, BridgeEventType send) {
        return WebsocketEventMessage.builder().type(send).address(address).body(body).build().toJson();
    }

}
