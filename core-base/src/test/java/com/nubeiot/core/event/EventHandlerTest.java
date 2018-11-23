package com.nubeiot.core.event;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.StateException;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class EventHandlerTest {

    @Test
    public void test_GetMethod_By_Value() {
        final Method method = EventHandler.getMethodByAnnotation(MockEventHandler.class, EventType.GET_LIST);
        Assert.assertNotNull(method);
        Assert.assertEquals("getList", method.getName());
    }

    @Test
    public void test_GetMethod_By_HaltValue() {
        final Method method = EventHandler.getMethodByAnnotation(MockEventHandler.class, EventType.HALT);
        Assert.assertNotNull(method);
        Assert.assertEquals("delete", method.getName());
    }

    @Test
    public void test_GetMethod_By_NotFoundValue() {
        final Method method = EventHandler.getMethodByAnnotation(MockEventHandler.class, EventType.INIT);
        Assert.assertNull(method);
    }

    @Test
    public void test_GetMethod_By_NonePrivateMethod() {
        final Method method = EventHandler.getMethodByAnnotation(MockEventHandler.class, EventType.UPDATE);
        Assert.assertNull(method);
    }

    @Test
    public void test_execute_method_remove() {
        final Single<JsonObject> handle = new MockEventHandler().handle(EventType.REMOVE,
                                                                        RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("delete", handle.blockingGet().getString("key"));
    }

    @Test
    public void test_execute_method_install() {
        final Single<JsonObject> handle = new MockEventHandler().handle(EventType.CREATE,
                                                                        RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("install", handle.blockingGet().getString("key"));
    }

    @Test(expected = StateException.class)
    public void test_execute_method_unsupported_event() {
        new MockEventHandler().handle(EventType.UPDATE, RequestData.builder().build());
    }

}
