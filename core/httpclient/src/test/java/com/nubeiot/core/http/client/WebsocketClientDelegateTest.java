package com.nubeiot.core.http.client;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.client.ws.WebsocketClientDelegate;

@RunWith(VertxUnitRunner.class)
public class WebsocketClientDelegateTest {

    private Vertx vertx;
    private HttpClientConfig config;

    @BeforeClass
    public static void beforeClass() {
        TestHelper.setup();
    }

    @Before
    public void setup() {
        vertx = Vertx.vertx();
        config = new HttpClientConfig(new HttpClientOptions().setWebsocketCompressionAllowClientNoContext(true)
                                                             .setWebsocketCompressionRequestServerNoContext(true));
    }

    @After
    public void teardown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void test(TestContext context) {
        Async async = context.async();
        WebsocketClientDelegate client = WebsocketClientDelegate.create(vertx, config);
        client.send(
            new RequestOptions().setHost("echo.websocket.org").setPort(443).setSsl(true).setURI("/echo"),
            RequestData.builder().body(new JsonObject().put("ab", "cd")).build()).subscribe();
    }

}
