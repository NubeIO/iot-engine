package com.nubeiot.core.http.dynamic;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.dynamic.mock.MockEventOneApiOneLocService;

@RunWith(VertxUnitRunner.class)
public class DynamicEventOneApiOneLocServiceTest extends DynamicServiceTestBase {

    @Override
    @SuppressWarnings("unchecked")
    protected MockEventOneApiOneLocService service() {
        return new MockEventOneApiOneLocService();
    }

    @Test
    public void test_event_not_found(TestContext context) {
        assertRestByClient(context, HttpMethod.POST, "/api/s/hey", 409,
                           new JsonObject().put("code", ErrorCode.STATE_ERROR)
                                           .put("message", "Unsupported event CREATE"));
    }

    @Test
    public void test_get_list_success(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/hey", 200,
                           new JsonObject().put("data", Arrays.asList("1", "2", "3")));
    }

    @Test
    public void test_get_one_success(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/hey/123", 200, new JsonObject().put("data", 123));
    }

    @Test
    public void test_not_found(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/xxx", 404,
                           new JsonObject("{\"message\":\"Resource not found\"}"), IGNORE_URI);
    }

    @Test
    public void test_get_list_multiple_success(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/c/123/p", 200,
                           new JsonObject().put("data", Collections.singletonList("123")));
    }

    @Test
    public void test_get_one_multiple_success(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/c/123/p/456", 200,
                           new JsonObject("{\"cId\":\"123\",\"pId\":\"456\"}"));
    }

    @Test
    public void test_get_list_not_use_request_data(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/x/123/y", 200,
                           new JsonObject().put("data", Collections.singletonList("123")));
    }

    @Test
    public void test_get_one_not_use_request_data(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/x/123/y/456", 200,
                           new JsonObject("{\"xId\":\"123\",\"yId\":\"456\"}"));
    }

    @Test
    public void test_get_gateway_index(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"apis\":[{\"name\":\"ems-3\",\"status\":\"UP\",\"location\":\"test.MockEventMessageService.3\"," +
            "\"endpoints\":[{\"method\":\"PATCH\",\"path\":\"/x/:xId/y/:yId\"},{\"method\":\"GET\"," +
            "\"path\":\"/x/:xId/y/:yId\"},{\"method\":\"POST\",\"path\":\"/x/:xId/y\"},{\"method\":\"DELETE\"," +
            "\"path\":\"/x/:xId/y/:yId\"},{\"method\":\"PUT\",\"path\":\"/x/:xId/y/:yId\"},{\"method\":\"GET\"," +
            "\"path\":\"/x/:xId/y\"}]},{\"name\":\"ems-1\",\"status\":\"UP\",\"location\":\"test" +
            ".MockEventMessageService.1\",\"endpoints\":[{\"method\":\"PATCH\",\"path\":\"/hey/:id\"}," +
            "{\"method\":\"GET\",\"path\":\"/hey/:id\"},{\"method\":\"POST\",\"path\":\"/hey\"}," +
            "{\"method\":\"DELETE\",\"path\":\"/hey/:id\"},{\"method\":\"PUT\",\"path\":\"/hey/:id\"}," +
            "{\"method\":\"GET\",\"path\":\"/hey\"}]},{\"name\":\"ems-2\",\"status\":\"UP\",\"location\":\"test" +
            ".MockEventMessageService.2\",\"endpoints\":[{\"method\":\"PATCH\",\"path\":\"/c/:cId/p/:pId\"}," +
            "{\"method\":\"GET\",\"path\":\"/c/:cId/p/:pId\"},{\"method\":\"POST\",\"path\":\"/c/:cId/p\"}," +
            "{\"method\":\"DELETE\",\"path\":\"/c/:cId/p/:pId\"},{\"method\":\"PUT\",\"path\":\"/c/:cId/p/:pId\"}," +
            "{\"method\":\"GET\",\"path\":\"/c/:cId/p\"}]}]}");
        assertRestByClient(context, HttpMethod.GET, "/gw/index", 200, expected, JSONCompareMode.LENIENT);
    }

}
