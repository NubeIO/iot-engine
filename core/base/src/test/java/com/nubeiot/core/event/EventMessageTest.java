package com.nubeiot.core.event;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

public class EventMessageTest {

    @Test
    public void test_EventMessage_Success() {
        EventMessage msg = EventMessage.success(EventAction.CREATE, new JsonObject(
                "{\"groupId\":\"io.nubespark\"," + "\"artifactId\":\"nube-edge-ditto-driver\"," +
                "\"version\":\"1.0-SNAPSHOT\"}"));
        Assert.assertFalse(msg.isError());
        Assert.assertTrue(msg.isSuccess());
        Assert.assertEquals(EventAction.CREATE, msg.getAction());
        Assert.assertNull(msg.getError());
        Assert.assertEquals("{\"status\":\"SUCCESS\",\"action\":\"CREATE\",\"data\":{\"groupId\":\"io.nubespark\"," +
                            "\"artifactId\":\"nube-edge-ditto-driver\",\"version\":\"1.0-SNAPSHOT\"},\"error\":null}",
                            msg.toJson().encode());
    }

    @Test
    public void test_EventMessage_Error() {
        EventMessage error = EventMessage.error(EventAction.REMOVE, new RuntimeException("xxx"));
        Assert.assertTrue(error.isError());
        Assert.assertFalse(error.isSuccess());
        Assert.assertEquals(EventAction.REMOVE, error.getAction());
        Assert.assertNotNull(error.getError());
        Assert.assertEquals("{}", error.getData().encode());
        Assert.assertEquals("{\"status\":\"FAILED\",\"action\":\"REMOVE\",\"data\":{}," +
                            "\"error\":{\"code\":\"UNKNOWN_ERROR\",\"message\":\"UNKNOWN_ERROR | Cause: xxx\"}}",
                            error.toJson().encode());
    }

    @Test
    public void parse_from_json() {
        final JsonObject jsonObject = new JsonObject("{\"action\":\"CREATE\",\"data\":{\"groupId\":\"io.nubespark\"," +
                                                     "\"artifactId\":\"nube-edge-ditto-driver\",\"version\":\"1" +
                                                     ".0-SNAPSHOT\"}}");
        EventMessage message = EventMessage.from(jsonObject.getMap());
        Assert.assertFalse(message.isError());
        Assert.assertTrue(message.isSuccess());
        Assert.assertEquals(EventAction.CREATE, message.getAction());
        Assert.assertNull(message.getError());
    }

}
