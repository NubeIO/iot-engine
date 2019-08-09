package com.nubeiot.core.http;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.skyscreamer.jsonassert.Customization;

import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpClient;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.http.ws.WebsocketEventMessage;
import com.nubeiot.core.utils.Strings;
import com.zandero.rest.RestRouter;

public class HttpServerTestBase {

    protected static final String DEFAULT_HOST = "127.0.0.1";
    protected Vertx vertx;
    protected HttpConfig httpConfig;
    protected HttpClient client;
    protected RequestOptions requestOptions;

    protected static void assertResponse(TestContext context, Async async, JsonObject expected,
                                         io.vertx.core.buffer.Buffer actual) {
        JsonHelper.assertJson(context, async, expected, actual.toJsonObject());
    }

    protected void before(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        httpConfig = IConfig.fromClasspath(httpConfigFile(), HttpConfig.class);
        httpConfig.setHost(DEFAULT_HOST);
        httpConfig.setPort(TestHelper.getRandomPort());
        client = vertx.createHttpClient(createClientOptions());
        requestOptions = new RequestOptions().setHost(DEFAULT_HOST).setPort(httpConfig.getPort());
    }

    protected void after(TestContext context) {
        RestRouter.getWriters().clear();
        RestRouter.getReaders().clear();
        RestRouter.getContextProviders().clear();
        RestRouter.getExceptionHandlers().clear();
        vertx.close(context.asyncAssertSuccess());
    }

    protected String httpConfigFile() {
        return "httpServer.json";
    }

    protected void enableWebsocket() {
        this.httpConfig.getRestConfig().setEnabled(false);
        this.httpConfig.getWebsocketConfig().setEnabled(true);
    }

    private HttpClientOptions createClientOptions() {
        return new HttpClientOptions().setConnectTimeout(TestHelper.TEST_TIMEOUT_SEC);
    }

    protected void assertRestByClient(TestContext context, HttpMethod method, String path, int codeExpected,
                                      JsonObject bodyExpected, Customization... customizations) {
        assertRestByClient(context, method, path, null, codeExpected, bodyExpected, customizations);
    }

    protected void assertRestByClient(TestContext context, HttpMethod method, String path, RequestData requestData,
                                      int codeExpected, JsonObject bodyExpected, Customization... customizations) {
        Async async = context.async();
        HttpClientDelegate.create(vertx.getDelegate(), HostInfo.from(requestOptions))
                          .execute(path, method, requestData)
                          .doFinally(() -> TestHelper.testComplete(async))
                          .subscribe(
                              resp -> assertResponse(context, codeExpected, bodyExpected, async, resp, customizations),
                              context::fail);
    }

    private void assertResponse(TestContext context, int codeExpected, JsonObject bodyExpected, Async async,
                                ResponseData resp, Customization[] customizations) {
        System.out.println("Client asserting...");
        System.out.println(resp.getStatus());
        try {
            context.assertEquals(HttpUtils.DEFAULT_CONTENT_TYPE,
                                 resp.headers().getString(HttpHeaders.CONTENT_TYPE.toString()));
            context.assertNotNull(resp.headers().getString("x-response-time"));
            context.assertEquals(codeExpected, resp.getStatus().code());
            JsonHelper.assertJson(context, async, bodyExpected, resp.body(), customizations);
        } catch (AssertionError e) {
            context.fail(e);
        }
    }

    protected Single<ResponseData> restRequest(TestContext context, HttpMethod method, String path,
                                               RequestData requestData) {
        Async async = context.async();
        return HttpClientDelegate.create(vertx.getDelegate(), HostInfo.from(requestOptions))
                                 .execute(path, method, requestData)
                                 .doFinally(() -> TestHelper.testComplete(async));
    }

    protected HttpServer startServer(TestContext context, HttpServerRouter httpRouter) {
        return VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions().setConfig(httpConfig.toJson()),
                                  new HttpServer(httpRouter));
    }

    protected void startServer(TestContext context, HttpServerRouter httpRouter, Handler<Throwable> consumer) {
        VertxHelper.deployFailed(vertx.getDelegate(), context, new DeploymentOptions().setConfig(httpConfig.toJson()),
                                 new HttpServer(httpRouter), consumer);
    }

    protected JsonObject notFoundResponse(int port, String path) {
        return new JsonObject().put("message", "Resource not found")
                               .put("uri", Strings.format("http://{0}:{1}{2}", DEFAULT_HOST, port, path));
    }

    protected void testComplete(Async async) {
        testComplete(async, "");
    }

    private void testComplete(Async async, String msgEvent) {
        TestHelper.testComplete(async, msgEvent, closeClient());
    }

    private Handler<Void> closeClient() {
        return e -> client.close();
    }

    protected void assertJsonData(Async async, String address, Consumer<Object> asserter) {
        EventbusHelper.assertReceivedData(vertx.getDelegate(), async, address, asserter, closeClient());
    }

    protected WebSocket setupSockJsClient(TestContext context, Async async, Consumer<Throwable> error)
        throws InterruptedException {
        return setupSockJsClient(context, async, "/ws", null, error);
    }

    protected WebSocket setupSockJsClient(TestContext context, Async async, String path,
                                          Consumer<WebSocket> writerBeforeHandler, Consumer<Throwable> error)
        throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<WebSocket> wsReference = new AtomicReference<>();
        client.websocket(requestOptions.setURI(Urls.combinePath(path, "websocket")), ws -> {
            if (Objects.nonNull(writerBeforeHandler)) {
                writerBeforeHandler.accept(ws.getDelegate());
            }
            wsReference.set(ws.getDelegate());
            latch.countDown();
            ws.endHandler(v -> testComplete(async, "CLIENT END"));
            ws.exceptionHandler(error::accept);
        }, error::accept);
        context.assertTrue(latch.await(TestHelper.TEST_TIMEOUT_SEC, TimeUnit.SECONDS), "Timeout");
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
