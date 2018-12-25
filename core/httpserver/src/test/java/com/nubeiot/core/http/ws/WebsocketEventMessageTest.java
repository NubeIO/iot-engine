package com.nubeiot.core.http.ws;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;

public class WebsocketEventMessageTest {

    @Test(expected = InitializerError.class)
    public void test_missing_address() {
        WebsocketEventMessage.builder().type(BridgeEventType.REGISTER).build();
    }

    @Test(expected = InitializerError.class)
    public void test_missing_type() {
        WebsocketEventMessage.builder().address("test").build();
    }

    @Test
    public void test_serialize_missing_msg() throws JSONException {
        WebsocketEventMessage message = WebsocketEventMessage.builder()
                                                             .address("test")
                                                             .type(BridgeEventType.REGISTER)
                                                             .build();
        JSONAssert.assertEquals("{\"address\":\"test\",\"type\":\"register\"}", message.toJson().encode(),
                                JSONCompareMode.STRICT);
    }

    @Test
    public void test_serialize_success() throws JSONException {
        EventMessage eventMessage = EventMessage.success(EventAction.CREATE, new JsonObject().put("hello", "world"));
        WebsocketEventMessage message = WebsocketEventMessage.builder()
                                                             .address("test")
                                                             .type(BridgeEventType.SEND)
                                                             .body(eventMessage)
                                                             .build();
        JSONAssert.assertEquals("{\"address\":\"test\",\"type\":\"send\",\"body\":{\"status\":\"SUCCESS\"," +
                                "\"action\":\"CREATE\",\"data\":{\"hello\":\"world\"}}}", message.toJson().encode(),
                                JSONCompareMode.STRICT);
    }

    @Test
    public void test_deserialize_missing_msg() {
        WebsocketEventMessage message = new JsonObject("{\"address\":\"test\",\"type\":\"REGISTER\"}").mapTo(
                WebsocketEventMessage.class);
        Assert.assertEquals("test", message.getAddress());
        Assert.assertEquals(BridgeEventType.REGISTER, message.getType());
        Assert.assertNull(message.getBody());
    }

    @Test(expected = NubeException.class)
    public void test_deserialize_socketMsg_missing_event_action() {
        WebsocketEventMessage.from(
                "{\"address\":\"test\",\"type\":\"SEND\",\"body\":{\"data\":{\"hello\":\"world\"}}}");
    }

    @Test(expected = NubeException.class)
    public void test_deserialize_socketMsg_unknown_type() {
        WebsocketEventMessage.from("{\"address\":\"test\",\"type\":\"xxx\"}");
    }

    @Test
    public void test_deserialize_socketMsg_without_data() {
        WebsocketEventMessage from = WebsocketEventMessage.from(
                new JsonObject("{\"address\":\"socket.client2server\",\"type\":\"RECEIVE\"}"));
        Assert.assertEquals(BridgeEventType.RECEIVE, from.getType());
        Assert.assertEquals("socket.client2server", from.getAddress());
        Assert.assertNull(from.getBody());
    }

    @Test
    public void test_deserialize_socketMsg_full() throws JSONException {
        WebsocketEventMessage message = WebsocketEventMessage.from(
                "{\"address\":\"test\",\"type\":\"rec\",\"body\":{" + "\"action\":\"CREATE\"," +
                "\"data\":{\"hello\":\"world\"}}}");
        Assert.assertEquals("test", message.getAddress());
        Assert.assertEquals(BridgeEventType.RECEIVE, message.getType());
        Assert.assertNotNull(message.getBody());
        Assert.assertEquals(EventAction.CREATE, message.getBody().getAction());
        Assert.assertEquals(Status.SUCCESS, message.getBody().getStatus());
        JSONAssert.assertEquals("{\"hello\":\"world\"}", message.getBody().getData().encode(), JSONCompareMode.STRICT);
        Assert.assertNull(message.getBody().getError());
    }

    @Test(expected = NubeException.class)
    public void test_deserialize_unknown_type() {
        WebsocketEventMessage.from("{\"address\":\"test\",\"type\":\"rec1\",\"body\":{" + "\"action\":\"CREATE\"}}");
    }

}