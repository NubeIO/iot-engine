package com.nubeio.iot.share.event;

import org.junit.Test;

import io.vertx.core.json.JsonObject;

public class EventMessageTest {

    @Test
    public void test_EventMessage_Success() {
        EventMessage msg = EventMessage.success(EventType.CREATE, new JsonObject(
                "{\"groupId\":\"io.nubespark\"," + "\"artifactId\":\"nube-edge-ditto-driver\"," +
                "\"version\":\"1.0-SNAPSHOT\"}"));
        System.out.println(msg.toJson());
        System.out.println(EventMessage.from(msg));
    }

    @Test
    public void test_EventMessage_Error() {
        EventMessage error = EventMessage.error(EventType.REMOVE, new RuntimeException("xxx"));
        System.out.println(error.toString());
        System.out.println(error.toJson());
        System.out.println(EventMessage.from(error));
    }

}
