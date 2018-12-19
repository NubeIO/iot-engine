package com.nubeiot.core.http;

import java.io.IOException;
import java.net.ServerSocket;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.utils.Strings;
import com.zandero.rest.RestRouter;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpClient;

public class BaseHttpServerTest {

    protected static final int DEFAULT_CONNECT_TIMEOUT = 3000;
    protected static final String DEFAULT_HOST = "127.0.0.1";
    protected Vertx vertx;
    protected JsonObject httpConfig;
    protected HttpClient client;
    private HttpServer httpServer;

    protected static void beforeSuite() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }

    protected void before(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        httpConfig = new JsonObject().put("port", getRandomPort()).put("host", DEFAULT_HOST);
        client = vertx.createHttpClient(new HttpClientOptions().setConnectTimeout(DEFAULT_CONNECT_TIMEOUT));
    }

    protected void after(TestContext context) {
        RestRouter.getWriters().clear();
        RestRouter.getReaders().clear();
        RestRouter.getContextProviders().clear();
        RestRouter.getExceptionHandlers().clear();
        httpServer.stop();
        vertx.close(context.asyncAssertSuccess());
    }

    protected int getRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    protected void assertByClient(TestContext context, HttpMethod method, int port, String path, int codeExpected,
                                  JsonObject bodyExpected) {
        Async async = context.async();
        client.request(method, port, DEFAULT_HOST, path).handler(resp -> {
            context.assertEquals(ApiConstants.DEFAULT_CONTENT_TYPE, resp.getHeader(ApiConstants.CONTENT_TYPE));
            //            context.assertNotNull(resp.getHeader("x-response-time"));
            context.assertEquals(codeExpected, resp.statusCode());
            resp.bodyHandler(body -> assertResponseBody(context, bodyExpected, body));
        }).endHandler(event -> {
            client.close();
            async.complete();
        }).end();
    }

    protected void startServer(HttpServerRouter httpRouter) {
        httpServer = new HttpServer(vertx, httpConfig, httpRouter);
        httpServer.start();
        try {
            Thread.sleep(1000);
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

}
