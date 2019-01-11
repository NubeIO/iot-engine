package com.nubeiot.core.event;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.exceptions.NubeException;

public class EventMessageTest {

    @Test
    public void test_EventMessage_Success() {
        EventMessage msg = EventMessage.success(EventAction.CREATE, new JsonObject(
            "{\"groupId\":\"io.nubespark\",\"version\":\"1.0-SNAPSHOT\"}"));
        Assert.assertFalse(msg.isError());
        Assert.assertTrue(msg.isSuccess());
        Assert.assertEquals(EventAction.CREATE, msg.getAction());
        Assert.assertNull(msg.getError());
        Assert.assertEquals("{\"status\":\"SUCCESS\",\"action\":\"CREATE\",\"" +
                            "data\":{\"groupId\":\"io.nubespark\",\"version\":\"1.0-SNAPSHOT\"}}",
                            msg.toJson().encode());
    }

    @Test
    public void test_EventMessage_Error() {
        EventMessage error = EventMessage.error(EventAction.REMOVE, new RuntimeException("xxx"));
        Assert.assertTrue(error.isError());
        Assert.assertFalse(error.isSuccess());
        Assert.assertEquals(EventAction.REMOVE, error.getAction());
        Assert.assertNotNull(error.getError());
        Assert.assertNull(error.getData());
        Assert.assertEquals("{\"status\":\"FAILED\",\"action\":\"REMOVE\"," +
                            "\"error\":{\"code\":\"UNKNOWN_ERROR\",\"message\":\"UNKNOWN_ERROR | Cause: xxx\"}}",
                            error.toJson().encode());
    }

    @Test(expected = NubeException.class)
    public void test_deserialize_missing_action() {
        EventMessage.from(new JsonObject("{\"data\":{\"groupId\":\"io.nubespark\"}}"));
    }

    @Test
    public void test_deserialize_success() {
        JsonObject jsonObject = new JsonObject("{\"action\":\"CREATE\",\"data\":{\"groupId\":\"io.nubespark\"," +
                                               "\"artifactId\":\"nube-edge-ditto-driver\"}}");
        EventMessage message = EventMessage.from(jsonObject.getMap());
        Assert.assertFalse(message.isError());
        Assert.assertTrue(message.isSuccess());
        Assert.assertEquals(EventAction.CREATE, message.getAction());
        Assert.assertEquals("{\"groupId\":\"io.nubespark\",\"artifactId\":\"nube-edge-ditto-driver\"}",
                            message.getData().encode());
        Assert.assertNull(message.getError());
    }

    @Test
    public void test_deserialize_success_none_data() {
        JsonObject jsonObject = new JsonObject("{\"status\":\"SUCCESS\",\"action\":\"CREATE\"}");
        EventMessage message = EventMessage.from(jsonObject.getMap());
        Assert.assertFalse(message.isError());
        Assert.assertTrue(message.isSuccess());
        Assert.assertEquals(EventAction.CREATE, message.getAction());
        Assert.assertNull(message.getData());
        Assert.assertNull(message.getError());
    }

    @Test
    public void test_deserialize_error_data() {
        JsonObject jsonObject = new JsonObject(
            "{\"status\":\"FAILED\",\"action\":\"REMOVE\",\"error\":{\"code\":\"UNKNOWN_ERROR\"," +
            "\"message\":\"UNKNOWN_ERROR | Cause: xxx\"}}");
        EventMessage message = EventMessage.from(jsonObject);
        Assert.assertTrue(message.isError());
        Assert.assertFalse(message.isSuccess());
        Assert.assertEquals(EventAction.REMOVE, message.getAction());
        Assert.assertNull(message.getData());
        Assert.assertEquals(NubeException.ErrorCode.UNKNOWN_ERROR, message.getError().getCode());
        Assert.assertEquals("UNKNOWN_ERROR | Cause: xxx", message.getError().getMessage());
        Assert.assertNull(message.getError().getThrowable());
    }

}
