package com.nubeiot.core.http;

import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.mock.MockWebsocketEvent;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.WebsocketRejectedException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.eventbus.MessageConsumer;

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
        this.httpConfig.put("enabled", false).put(HttpServer.SOCKET_CFG_NAME, new JsonObject().put("enabled", true));
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
    public void test_not_found(TestContext context) {
        Async async = context.async();
        this.requestOptions.setURI("/socket1");
        startServer(new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.DEFAULT_METADATA));
        client.websocket(requestOptions, websocket -> testComplete(async),
                         t -> connectFailureHandler(context, async, t, HttpResponseStatus.NOT_FOUND));
    }

    @Test
    public void test_send_success(TestContext context) {
        this.requestOptions.setURI("/ws/websocket");
        EventMessage message = EventMessage.success(EventAction.GET_LIST, new JsonObject().put("echo", 1));
        JsonObject expected = new JsonObject().put("data", Arrays.asList("1", "2", "3"));
        startServer(new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.DEFAULT_METADATA));
        testSend(context, requestOptions, MockWebsocketEvent.DEFAULT_METADATA, message.toJson(), expected);
    }

    @Test
    public void test_send_failure(TestContext context) {
        this.requestOptions.setURI("/ws/websocket");
        JsonObject expected = EventMessage.error(EventAction.RETURN, NubeException.ErrorCode.INVALID_ARGUMENT,
                                                 "Message format is not correct").toJson();
        startServer(new HttpServerRouter().registerEventBusSocket(MockWebsocketEvent.DEFAULT_METADATA));
        testSend(context, requestOptions, MockWebsocketEvent.DEFAULT_METADATA, "xx", expected);
    }

    private void connectFailureHandler(TestContext context, Async async, Throwable t, HttpResponseStatus expected) {
        if (t instanceof WebsocketRejectedException) {
            WebsocketRejectedException websocketError = (WebsocketRejectedException) t;
            context.assertEquals(expected.code(), websocketError.getStatus());
        }
        testComplete(async);
    }

    private void testSend(TestContext context, RequestOptions options, WebsocketEventMetadata metadata, Object sendBody,
                          Object expected) {
        Async async = context.async();
        client.websocket(options, ws -> {
            String listenAddress = metadata.getPublisher().getAddress();
            String pushAddress = metadata.getListener().getAddress();
            MessageConsumer<Object> consumer = vertx.eventBus().consumer(listenAddress);
            consumer.handler(msg -> {
                System.out.println("successssssssss");
                Object receivedBody = msg.body();
                context.assertEquals(expected, receivedBody);
                consumer.unregister(v -> testComplete(async));
            });
            ws.exceptionHandler(t -> {
                System.out.println("errorrrrrrrrrrr");
                t.printStackTrace();
                //                context.assertTrue(t instanceof CorruptedFrameException);
                //                context.assertEquals(expected, ((CorruptedFrameException) t).getErrorCode());
            });
            JsonObject msg = new JsonObject().put("type", "send").put("address", pushAddress).put("body", sendBody);
            //            ws.writeFrame(WebSocketFrame.textFrame(msg.encode(), true));
            ws.write(Buffer.buffer(msg.encode()));
            ws.closeHandler(v -> testComplete(async));
        });
    }

    private void testSendFailure(TestContext context, RequestOptions options, WebsocketEventMetadata metadata,
                                 Object body, NubeException.ErrorCode expected) {
        Async async = context.async();
        client.websocket(options, ws -> {
            String listenAddress = metadata.getPublisher().getAddress();
            String pushAddress = metadata.getListener().getAddress();
            MessageConsumer<Object> consumer = vertx.eventBus().consumer(listenAddress);
            consumer.handler(msg -> {
                System.out.println("successssssssss");
                Object receivedBody = msg.body();
                context.assertEquals(body, receivedBody);
                consumer.unregister(v -> testComplete(async));
            });
            ws.exceptionHandler(t -> {
                System.out.println("errorrrrrrrrrrr");
                t.printStackTrace();
                //                context.assertTrue(t instanceof CorruptedFrameException);
                //                context.assertEquals(expected, ((CorruptedFrameException) t).getErrorCode());
            });
            JsonObject msg = new JsonObject().put("type", "send").put("address", pushAddress).put("body", body);
            ws.write(Buffer.buffer(msg.encode()));
            ws.closeHandler(v -> testComplete(async));
        }, t -> connectFailureHandler(context, async, t, HttpResponseStatus.NOT_FOUND));
    }

}
