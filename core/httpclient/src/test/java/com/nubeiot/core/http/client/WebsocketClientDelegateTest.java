package com.nubeiot.core.http.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.HttpException;
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
    public Timeout timeout = Timeout.seconds(TestHelper.TEST_TIMEOUT_SEC);
    private Vertx vertx;
    private HttpClientConfig config;
    private WebsocketClientDelegate client;
    private EventController controller;

    @BeforeClass
    public static void beforeClass() {
        TestHelper.setup();
    }

    @Before
    public void setup() {
        vertx = Vertx.vertx();
        config = new HttpClientConfig(new HttpClientOptions().setWebsocketCompressionAllowClientNoContext(true)
                                                             .setWebsocketCompressionRequestServerNoContext(true));
        controller = new EventController(vertx);
        client = WebsocketClientDelegate.create(vertx, config);
    }

    @After
    public void teardown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(expected = HttpException.class)
    public void test_connect_failed_to_unknown_dns() {
        client = WebsocketClientDelegate.create(vertx, config);
        RequestOptions opt = new RequestOptions().setHost("echo.websocket.orgx")
                                                 .setPort(443)
                                                 .setSsl(true)
                                                 .setURI("/echo");
        client.open(WebsocketClientEventMetadata.create(LISTENER, PUBLISHER_ADDRESS), opt, null);
    }

    @Test
    public void test_connect_and_send(TestContext context) throws InterruptedException {
        Async async = context.async();
        RequestOptions opt = new RequestOptions().setHost("echo.websocket.org")
                                                 .setPort(443)
                                                 .setSsl(true)
                                                 .setURI("/echo");
        controller.register(LISTENER, new EventAsserter(LISTENER, context, async, new JsonObject().put("k", 1)));
        client = WebsocketClientDelegate.create(vertx, config);
        client.open(WebsocketClientEventMetadata.create(LISTENER, PUBLISHER_ADDRESS), opt, null);
        Thread.sleep(1000);
        controller.request(PUBLISHER_ADDRESS, EventPattern.PUBLISH_SUBSCRIBE,
                           EventMessage.initial(EventAction.SEND, new JsonObject().put("k", 1)));
    }

    @RequiredArgsConstructor
    static class EventAsserter implements EventHandler {

        private final EventModel eventModel;
        private final TestContext context;
        private final Async async;
        private final JsonObject expected;

        @EventContractor(action = EventAction.UNKNOWN, returnType = int.class)
        public int send(JsonObject data) {
            context.assertEquals(expected, data);
            TestHelper.testComplete(async);
            return 1;
        }

        @Override
        public @NonNull List<EventAction> getAvailableEvents() {
            return new ArrayList<>(eventModel.getEvents());
        }

    }

}
