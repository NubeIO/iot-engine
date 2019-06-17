package com.nubeiot.core.http.dynamic;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.dynamic.mock.MockEventMessageService;

@RunWith(VertxUnitRunner.class)
public class DynamicEventServiceTest extends DynamicServiceTestBase {

    @BeforeClass
    public static void beforeSuite() { TestHelper.setup(); }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        startGatewayAndService(context, new MockEventMessageService(), new DeploymentOptions());
    }

    @After
    public void after(TestContext context) {
        super.after(context);
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
        assertRestByClient(context, HttpMethod.GET, "/api/s/xxx", 404, new JsonObject());
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

}
