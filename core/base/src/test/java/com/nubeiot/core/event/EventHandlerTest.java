package com.nubeiot.core.event;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.StateException;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class EventHandlerTest {

    @Test
    public void test_get_method_one_contractor() {
        final Method method = EventHandler.getMethodByAnnotation(MockEventHandler.class, EventType.CREATE);
        Assert.assertNotNull(method);
        Assert.assertEquals("install", method.getName());
    }

    @Test
    public void test_execute_method_one_contractor() {
        final Single<JsonObject> handle = new MockEventHandler().handle(EventType.CREATE,
                                                                        RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("install", handle.blockingGet().getString("key"));
    }

    @Test
    public void test_get_method_with_multiple_contractor() {
        final Method method1 = EventHandler.getMethodByAnnotation(MockEventHandler.class, EventType.HALT);
        final Method method2 = EventHandler.getMethodByAnnotation(MockEventHandler.class, EventType.REMOVE);
        Assert.assertNotNull(method1);
        Assert.assertNotNull(method2);
        Assert.assertEquals("delete", method1.getName());
        Assert.assertEquals("delete", method2.getName());
    }

    @Test
    public void test_execute_method_with_multiple_contractor() {
        final Single<JsonObject> handle = new MockEventHandler().handle(EventType.REMOVE,
                                                                        RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("delete", handle.blockingGet().getString("key"));
    }

    @Test
    public void test_get_method_NotFoundValue() {
        Assert.assertNull(EventHandler.getMethodByAnnotation(MockEventHandler.class, EventType.INIT));
    }

    @Test(expected = StateException.class)
    public void test_execute_method_unsupported_event() {
        new MockEventHandler().handle(EventType.GET_LIST, RequestData.builder().build());
    }

    @Test
    public void test_get_method_PrivateStaticMethod() {
        Assert.assertNull(EventHandler.getMethodByAnnotation(MockEventHandler.class, EventType.INIT));
    }

    @Test
    public void test_get_method_NonePrivateMethod() {
        Assert.assertNull(EventHandler.getMethodByAnnotation(MockEventHandler.class, EventType.GET_ONE));
    }

    @Test(expected = NubeException.class)
    public void test_execute_method_that_throwException() {
        new MockEventHandler().handle(EventType.UPDATE, RequestData.builder().build());
    }

}
