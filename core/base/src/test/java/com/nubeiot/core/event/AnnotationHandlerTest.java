package com.nubeiot.core.event;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.MockEventHandler.MockEventUnsupportedHandler;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.StateException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class AnnotationHandlerTest {

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.TRACE);
    }

    private AnnotationHandler<MockEventHandler> getHandler() {
        return new AnnotationHandler<>(new MockEventHandler());
    }

    @Test
    public void test_get_method_one_contractor() {
        Method method = AnnotationHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.UPDATE).getMethod();
        Assert.assertNotNull(method);
        Assert.assertEquals("throwException", method.getName());
    }

    @Test
    public void test_execute_method_contractor_return_other() {
        Single<JsonObject> handle = getHandler().execute(EventAction.CREATE, RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("install", handle.blockingGet().getString("key"));
    }

    @Test
    public void test_execute_method_contractor_return_single_json() {
        Single<JsonObject> handle = getHandler().execute(EventAction.INIT, RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("init", handle.blockingGet().getString("key"));
    }

    @Test
    public void test_execute_method_contractor_return_single_other() {
        Single<JsonObject> handle = getHandler().execute(EventAction.GET_LIST, RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("list", handle.blockingGet().getString("key"));
    }

    @Test
    public void test_get_method_with_multiple_contractor() {
        Method method1 = AnnotationHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.HALT).getMethod();
        Method method2 = AnnotationHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.REMOVE)
                                          .getMethod();
        Assert.assertNotNull(method1);
        Assert.assertNotNull(method2);
        Assert.assertEquals("delete", method1.getName());
        Assert.assertEquals("delete", method2.getName());
    }

    @Test
    public void test_execute_method_with_multiple_contractor() {
        final Single<JsonObject> handle = getHandler().execute(EventAction.REMOVE, RequestData.builder().build());
        Assert.assertNotNull(handle);
        Assert.assertEquals("delete", handle.blockingGet().getString("key"));
    }

    @Test(expected = ImplementationError.class)
    public void test_get_method_no_output() {
        AnnotationHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.GET_ONE);
    }

    @Test(expected = StateException.class)
    public void test_execute_method_unsupported_event() {
        new AnnotationHandler<>(new MockEventUnsupportedHandler()).execute(EventAction.GET_LIST,
                                                                           RequestData.builder().build());
    }

    @Test(expected = ImplementationError.class)
    public void test_get_method_public_static() {
        AnnotationHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.GET_ONE);
    }

    @Test(expected = ImplementationError.class)
    public void test_get_method_none_public_method() {
        AnnotationHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.GET_ONE);
    }

    @Test(expected = RuntimeException.class)
    public void test_execute_method_that_throwException() {
        getHandler().execute(EventAction.UPDATE, RequestData.builder().build());
    }

}
