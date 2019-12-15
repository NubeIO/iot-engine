package com.nubeiot.core.http.client;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.base.HostInfo;
import com.nubeiot.core.http.base.event.WebsocketClientEventMetadata;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RunWith(VertxUnitRunner.class)
public class WebsocketClientDelegateTest {

    private static final EventModel LISTENER = EventModel.builder()
                                                         .address("ws.listener")
                                                         .local(true)
                                                         .pattern(EventPattern.POINT_2_POINT)
                                                         .addEvents(EventAction.UNKNOWN)
                                                         .build();
    private static final String PUBLISHER_ADDRESS = "ws.publisher";

    @Rule
    public Timeout timeout = Timeout.seconds(TestHelper.TEST_TIMEOUT_SEC * 2);
    private Vertx vertx;
    private HttpClientConfig config;
    private HostInfo hostInfo;

    @BeforeClass
    public static void beforeClass() {
        TestHelper.setup();
    }

    @Before
    public void setup() {
        vertx = Vertx.vertx();
        config = new HttpClientConfig();
        hostInfo = HostInfo.builder().host("echo.websocket.org").port(443).ssl(true).build();
    }

    @After
    public void teardown(TestContext context) {
        HttpClientRegistry.getInstance().clear();
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(expected = HttpException.class)
    public void test_connect_failed_due_unknown_dns() {
        config.getOptions().setConnectTimeout(6 * 1000);
        HostInfo opt = HostInfo.builder().host("echo.websocket.test").port(443).ssl(true).build();
        WebsocketClientDelegate client = WebsocketClientDelegate.create(vertx, config, opt);
        client.open(WebsocketClientEventMetadata.create("/echo", LISTENER, PUBLISHER_ADDRESS), null);
    }

    @Test
    public void test_connect_and_send(TestContext context) throws InterruptedException {
        Async async = context.async();
        WebsocketClientDelegate client = WebsocketClientDelegate.create(vertx, config, hostInfo);
        client.getEventController()
              .register(LISTENER, new EventAsserter(LISTENER, context, async, new JsonObject().put("k", 1)));
        client.open(WebsocketClientEventMetadata.create("/echo", LISTENER, PUBLISHER_ADDRESS), null);
        Thread.sleep(1000);
        client.getEventController()
              .request(PUBLISHER_ADDRESS, EventPattern.PUBLISH_SUBSCRIBE,
                       EventMessage.initial(EventAction.SEND, new JsonObject().put("k", 1)));
    }

    @Test(expected = HttpException.class)
    public void test_not_found(TestContext context) {
        WebsocketClientDelegate client = WebsocketClientDelegate.create(vertx, config, hostInfo);
        try {
            client.open(WebsocketClientEventMetadata.create("/xxx", LISTENER, PUBLISHER_ADDRESS), null);
        } catch (HttpException ex) {
            context.assertEquals(HttpResponseStatus.NOT_FOUND, ex.getStatusCode());
            context.assertEquals(ErrorCode.NOT_FOUND, ((NubeException) ex.getCause()).getErrorCode());
            throw ex;
        }
    }

    @Test
    public void test_cache(TestContext context) throws InterruptedException {
        context.assertTrue(HttpClientRegistry.getInstance().getWsRegistries().isEmpty());

        final WebsocketClientDelegate client1 = WebsocketClientDelegate.create(vertx, config, hostInfo);
        context.assertEquals(1, HttpClientRegistry.getInstance().getWsRegistries().size());
        context.assertEquals(1, HttpClientRegistry.getInstance().getWsRegistries().get(hostInfo).current());

        final WebsocketClientDelegate client2 = WebsocketClientDelegate.create(vertx, config, hostInfo);
        context.assertEquals(1, HttpClientRegistry.getInstance().getWsRegistries().size());
        context.assertEquals(2, HttpClientRegistry.getInstance().getWsRegistries().get(hostInfo).current());

        final HostInfo host2 = HostInfo.builder().host("echo.websocket.google").build();
        final WebsocketClientDelegate client3 = WebsocketClientDelegate.create(vertx, config, host2);
        context.assertEquals(2, HttpClientRegistry.getInstance().getWsRegistries().size());
        context.assertEquals(1, HttpClientRegistry.getInstance().getWsRegistries().get(host2).current());

        client1.open(WebsocketClientEventMetadata.create("/echo", LISTENER, PUBLISHER_ADDRESS), null);
        client2.open(WebsocketClientEventMetadata.create("/echo", LISTENER, PUBLISHER_ADDRESS), null);
        try {
            client3.open(WebsocketClientEventMetadata.create("/echo", LISTENER, PUBLISHER_ADDRESS), null);
        } catch (HttpException e) {
            context.assertEquals(1, HttpClientRegistry.getInstance().getWsRegistries().size());
        }
        client1.close();
        client2.close();

        Thread.sleep(1000);
        context.assertTrue(HttpClientRegistry.getInstance().getWsRegistries().isEmpty());
    }

    @RequiredArgsConstructor
    static class EventAsserter implements EventListener {

        private final EventModel eventModel;
        private final TestContext context;
        private final Async async;
        private final JsonObject expected;

        @EventContractor(action = EventAction.UNKNOWN, returnType = int.class)
        public int send(JsonObject data) {
            JsonHelper.assertJson(context, async, expected, data);
            return 1;
        }

        @Override
        public @NonNull Collection<EventAction> getAvailableEvents() {
            return new ArrayList<>(eventModel.getEvents());
        }

    }

}
