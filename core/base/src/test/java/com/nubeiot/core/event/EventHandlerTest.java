package com.nubeiot.core.event;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.StateException;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class EventHandlerTest {

    @Test
    public void test_get_method_one_contractor() {
        final Method method = EventHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.UPDATE);
        Assert.assertNotNull(method);
        Assert.assertEquals("throwException", method.getName());
    }

    @Test
    public void test_execute_method_contractor_return_other() {
        Single<JsonObject> handle = new MockEventHandler().handleEvent(EventAction.CREATE,
                                                                       RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("install", handle.blockingGet().getString("key"));
    }

    @Test
    public void test_execute_method_contractor_return_single_json() {
        Single<JsonObject> handle = new MockEventHandler().handleEvent(EventAction.INIT, RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("init", handle.blockingGet().getString("key"));
    }

    @Test
    public void test_execute_method_contractor_return_single_other() {
        Single<JsonObject> handle = new MockEventHandler().handleEvent(EventAction.GET_LIST,
                                                                       RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("list", handle.blockingGet().getString("key"));
    }

    @Test
    public void test_get_method_with_multiple_contractor() {
        final Method method1 = EventHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.HALT);
        final Method method2 = EventHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.REMOVE);
        Assert.assertNotNull(method1);
        Assert.assertNotNull(method2);
        Assert.assertEquals("delete", method1.getName());
        Assert.assertEquals("delete", method2.getName());
    }

    @Test
    public void test_execute_method_with_multiple_contractor() {
        final Single<JsonObject> handle = new MockEventHandler().handleEvent(EventAction.REMOVE,
                                                                             RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("delete", handle.blockingGet().getString("key"));
    }

    @Test(expected = HiddenException.ImplementationError.class)
    public void test_get_method_no_output() throws Throwable {
        try {
            EventHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.GET_ONE);
        } catch (NubeException e) {
            throw e.getCause();
        }
    }

    @Test(expected = StateException.class)
    public void test_execute_method_unsupported_event() {
        new MockEventHandler.MockEventUnsupportedHandler().handleEvent(EventAction.GET_LIST,
                                                                       RequestData.builder().build());
    }

    @Test(expected = HiddenException.ImplementationError.class)
    public void test_get_method_public_static() throws Throwable {
        try {
            EventHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.GET_ONE);
        } catch (NubeException e) {
            throw e.getCause();
        }
    }

    @Test(expected = HiddenException.ImplementationError.class)
    public void test_get_method_none_public_method() throws Throwable {
        try {
            EventHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.GET_ONE);
        } catch (NubeException e) {
            throw e.getCause();
        }
    }

    @Test(expected = RuntimeException.class)
    public void test_execute_method_that_throwException() {
        new MockEventHandler().handleEvent(EventAction.UPDATE, RequestData.builder().build());
    }

}
