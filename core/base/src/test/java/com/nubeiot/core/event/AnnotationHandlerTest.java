package com.nubeiot.core.event;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.MockEventHandler.MockEventUnsupportedHandler;
import com.nubeiot.core.event.MockEventHandler.MockEventWithDiffParam;
import com.nubeiot.core.event.MockEventHandler.MockParam;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.StateException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class AnnotationHandlerTest {

    private static Supplier<AnnotationHandler<MockEventHandler>> MH = () -> new AnnotationHandler<>(
        new MockEventHandler());
    private static Supplier<AnnotationHandler<MockEventUnsupportedHandler>> MEH = () -> new AnnotationHandler<>(
        new MockEventUnsupportedHandler());
    private static Supplier<AnnotationHandler<MockEventWithDiffParam>> MPH = () -> new AnnotationHandler<>(
        new MockEventWithDiffParam());

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.TRACE);
    }

    @Test
    public void test_get_method_one_contractor() {
        Method method = AnnotationHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.UPDATE).getMethod();
        Assert.assertNotNull(method);
        Assert.assertEquals("throwException", method.getName());
    }

    @Test(expected = ImplementationError.class)
    public void test_get_method_public_static() {
        AnnotationHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.GET_ONE);
    }

    @Test(expected = ImplementationError.class)
    public void test_get_method_none_public_method() {
        AnnotationHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.GET_ONE);
    }

    @Test(expected = ImplementationError.class)
    public void test_get_method_no_output() {
        AnnotationHandler.getMethodByAnnotation(MockEventHandler.class, EventAction.GET_ONE);
    }

    @Test
    public void test_execute_method_contractor_return_other() {
        Single<JsonObject> r = MH.get().execute(createMsgRequestData(EventAction.CREATE));
        Assert.assertNotNull(r);
        Assert.assertEquals("install", r.blockingGet().getString("key"));
    }

    @Test
    public void test_execute_method_contractor_return_single_json() {
        Single<JsonObject> r = MH.get().execute(createMsgRequestData(EventAction.INIT));
        Assert.assertNotNull(r);
        Assert.assertEquals("init", r.blockingGet().getString("key"));
    }

    @Test
    public void test_execute_method_contractor_return_single_other() {
        Single<JsonObject> r = MH.get().execute(createMsgRequestData(EventAction.GET_LIST));
        Assert.assertNotNull(r);
        Assert.assertEquals("list", r.blockingGet().getString("key"));
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
        Single<JsonObject> r = MH.get().execute(createMsgRequestData(EventAction.REMOVE));
        Assert.assertNotNull(r);
        Assert.assertEquals("delete", r.blockingGet().getString("key"));
    }

    @Test(expected = RuntimeException.class)
    public void test_execute_method_that_throwException() {
        MH.get().execute(createMsgRequestData(EventAction.UPDATE));
    }

    @Test(expected = StateException.class)
    public void test_execute_method_unsupported_event() {
        MEH.get().execute(createMsgRequestData(EventAction.GET_LIST));
    }

    private EventMessage createMsgRequestData(EventAction action) {
        return EventMessage.initial(action, RequestData.builder().build());
    }

    @Test
    public void test_no_param() {
        Single<JsonObject> r = MPH.get().execute(EventMessage.initial(EventAction.GET_LIST));
        Assert.assertEquals("hello", r.blockingGet().getString("data"));
    }

    @Test
    public void test_one_javaTypeParam() {
        Single<JsonObject> r = MPH.get()
                                  .execute(EventMessage.initial(EventAction.GET_ONE, new JsonObject().put("id", "1")));
        Assert.assertEquals(1, r.blockingGet().getInteger("data").intValue());
    }

    @Test
    public void test_one_refParam() {
        EventMessage msg = EventMessage.initial(EventAction.CREATE,
                                                RequestData.builder().body(new JsonObject().put("id", 1)).build());
        JsonObject r = MPH.get().execute(msg).blockingGet();
        RequestData from = JsonData.from(r, RequestData.class);
        Assert.assertEquals(1, from.getBody().getInteger("id").intValue());
    }

    @Test
    public void test_one_override_RefParam() {
        EventMessage msg = EventMessage.initial(EventAction.PATCH,
                                                RequestData.builder().body(new JsonObject().put("key", "1")).build());
        JsonObject r = MPH.get().execute(msg).blockingGet();
        RequestData from = JsonData.from(r, RequestData.class);
        Assert.assertEquals("1", from.getBody().getString("key"));
    }

    @Test
    public void test_two_RefParam() {
        JsonObject d = new JsonObject().put("mock", JsonObject.mapFrom(new MockParam(1, "hey")))
                                       .put("data", RequestData.builder()
                                                               .body(new JsonObject().put("o", "o"))
                                                               .build()
                                                               .toJson());
        EventMessage msg = EventMessage.initial(EventAction.UPDATE, d);
        JsonObject r = MPH.get().execute(msg).blockingGet();
        MockParam mock = r.getJsonObject("param").mapTo(MockParam.class);
        RequestData from = JsonData.from(r.getValue("request"), RequestData.class);
        Assert.assertEquals(1, mock.getId());
        Assert.assertEquals("hey", mock.getName());
        Assert.assertEquals("o", from.getBody().getValue("o"));
    }

    @Test
    public void test_mixParam() {
        JsonObject d = new JsonObject().put("id", 10)
                                       .put("data", RequestData.builder()
                                                               .body(new JsonObject().put("o", "o"))
                                                               .build()
                                                               .toJson());
        EventMessage msg = EventMessage.initial(EventAction.REMOVE, d);
        JsonObject r = MPH.get().execute(msg).blockingGet();
        Assert.assertEquals(10, r.getValue("id"));
        RequestData from = JsonData.from(r.getValue("request"), RequestData.class);
        Assert.assertEquals("o", from.getBody().getString("o"));
    }

}
