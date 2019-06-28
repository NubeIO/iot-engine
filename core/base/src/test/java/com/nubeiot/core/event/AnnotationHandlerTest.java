package com.nubeiot.core.event;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.LoggerFactory;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.MockEventListener.MockEventUnsupportedListener;
import com.nubeiot.core.event.MockEventListener.MockEventWithDiffParam;
import com.nubeiot.core.event.MockEventListener.MockParam;
import com.nubeiot.core.exceptions.HiddenException.ImplementationError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.exceptions.StateException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class AnnotationHandlerTest {

    private static Supplier<AnnotationHandler<MockEventListener>> MH = () -> new AnnotationHandler<>(
        new MockEventListener());
    private static Supplier<AnnotationHandler<MockEventUnsupportedListener>> MEH = () -> new AnnotationHandler<>(
        new MockEventUnsupportedListener());
    private static Supplier<AnnotationHandler<MockEventWithDiffParam>> MPH = () -> new AnnotationHandler<>(
        new MockEventWithDiffParam());

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.TRACE);
    }

    @Test
    public void test_get_method_one_contractor() {
        Method method = AnnotationHandler.getMethodByAnnotation(MockEventListener.class, EventAction.UPDATE)
                                         .getMethod();
        Assert.assertNotNull(method);
        Assert.assertEquals("throwException", method.getName());
    }

    @Test(expected = NubeException.class)
    public void test_data_is_null() {
        MH.get().execute(EventMessage.error(EventAction.GET_LIST, ErrorCode.EVENT_ERROR, "Invalid status"));
    }

    @Test(expected = NubeException.class)
    public void test_invalid_status() {
        MH.get().execute(EventMessage.error(EventAction.GET_LIST, ErrorCode.EVENT_ERROR, "Invalid status"));
    }

    @Test(expected = ImplementationError.class)
    public void test_get_method_public_static() {
        AnnotationHandler.getMethodByAnnotation(MockEventListener.class, EventAction.GET_ONE);
    }

    @Test(expected = ImplementationError.class)
    public void test_more_than_one_method_defined() {
        AnnotationHandler.getMethodByAnnotation(MockEventListener.class, EventAction.RETURN);
    }

    @Test(expected = ImplementationError.class)
    public void test_get_method_none_public_method() {
        AnnotationHandler.getMethodByAnnotation(MockEventListener.class, EventAction.MIGRATE);
    }

    @Test(expected = ImplementationError.class)
    public void test_get_method_no_output() {
        AnnotationHandler.getMethodByAnnotation(MockEventListener.class, EventAction.PATCH);
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
        Method method1 = AnnotationHandler.getMethodByAnnotation(MockEventListener.class, EventAction.HALT).getMethod();
        Method method2 = AnnotationHandler.getMethodByAnnotation(MockEventListener.class, EventAction.REMOVE)
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
        Assert.assertEquals(1, from.body().getInteger("id").intValue());
    }

    @Test
    public void test_one_override_RefParam() {
        EventMessage msg = EventMessage.initial(EventAction.PATCH,
                                                RequestData.builder().body(new JsonObject().put("key", "1")).build());
        JsonObject r = MPH.get().execute(msg).blockingGet();
        RequestData from = JsonData.from(r, RequestData.class);
        Assert.assertEquals("1", from.body().getString("key"));
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
        Assert.assertEquals("o", from.body().getValue("o"));
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
        Assert.assertEquals("o", from.body().getString("o"));
    }

    @Test
    public void test_collectionParam() {
        JsonObject d = new JsonObject().put("list", Arrays.asList("one", "two"));
        EventMessage msg = EventMessage.initial(EventAction.HALT, d);
        JsonObject r = MPH.get().execute(msg).blockingGet();
        Assert.assertEquals("one", r.getString("one"));
        Assert.assertEquals("two", r.getString("two"));
    }

    @Test(expected = ImplementationError.class)
    public void test_wrong_return_type() {
        AnnotationHandler.getMethodByAnnotation(MockEventWithDiffParam.class, EventAction.RETURN);
    }

    @Test(expected = ImplementationError.class)
    public void test_annotated_type_extends_return_type() {
        AnnotationHandler.getMethodByAnnotation(MockEventWithDiffParam.class, EventAction.INIT);
    }

    @Test
    public void test_return_type_extends_annotated_type() {
        AnnotationHandler.getMethodByAnnotation(MockEventWithDiffParam.class, EventAction.MIGRATE);
    }

    @Test
    public void test_json_param() throws JSONException {
        JsonObject data = new JsonObject(
            "{\"metadata" + "\":{\"service_name\":\"bios-installer\",\"version\":\"1.0.0-SNAPSHOT\"," + "\"state" +
            "\":\"ENABLED\"}," + "\"appConfig" + "\":{}," + "\"service_id" + "\":\"com.nubeiot" + ".edge" + ".module" +
            ":installer\"}");
        Single<JsonObject> response = MPH.get().execute(EventMessage.initial(EventAction.UNKNOWN, data));

        JSONAssert.assertEquals(
            "{\"service_name\":\"bios-installer\",\"version\":\"1.0.0-SNAPSHOT\"," + "\"state\":\"ENABLED\"}",
            response.blockingGet().toString(), JSONCompareMode.STRICT);
    }

}
