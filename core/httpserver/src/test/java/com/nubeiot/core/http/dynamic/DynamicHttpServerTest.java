package com.nubeiot.core.http.dynamic;

import java.io.IOException;

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
import com.nubeiot.core.http.dynamic.mock.HttpServiceServer;

@RunWith(VertxUnitRunner.class)
public class DynamicHttpServerTest extends DynamicServiceTestBase {

    @BeforeClass
    public static void beforeSuite() { TestHelper.setup(); }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        startGatewayAndService(context, new HttpServiceServer(),
                               new DeploymentOptions().setConfig(overridePort(TestHelper.getRandomPort())));
    }

    @After
    public void after(TestContext context) { super.after(context); }

    @Test
    public void test_get_success(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/rest/test", 200, new JsonObject().put("hello", "dynamic"));
    }

    @Test
    public void test_error(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/rest/test/error", 500,
                           new JsonObject().put("code", ErrorCode.UNKNOWN_ERROR)
                                           .put("message", new JsonObject().put("code", ErrorCode.UNKNOWN_ERROR)
                                                                           .put("message", "error")));
    }

    @Test
    public void test_not_found(TestContext context) {
        JsonObject m = new JsonObject().put("message", "Resource not found");
        assertRestByClient(context, HttpMethod.GET, "/api/s/rest/xxx", 404,
                           new JsonObject().put("code", ErrorCode.NOT_FOUND).put("message", m), IGNORE_URI);
    }

}
