package com.nubeiot.core.http.dynamic;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.dynamic.mock.MockHttpServiceServer;

@RunWith(VertxUnitRunner.class)
public class DynamicHttpServerTest extends DynamicServiceTestBase {

    private int port;

    @BeforeClass
    public static void beforeSuite() { TestHelper.setup(); }

    protected DeploymentOptions getServiceOptions() throws IOException {
        port = TestHelper.getRandomPort();
        return new DeploymentOptions().setConfig(deployConfig(port));
    }

    @Override
    protected <T extends ContainerVerticle> T service() {
        return (T) new MockHttpServiceServer();
    }

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
                           new JsonObject().put("code", ErrorCode.NOT_FOUND).put("message", m),
                           JsonHelper.ignore("message.uri"));
    }

    @Test
    public void test_get_gateway_index(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"apis\":[{\"name\":\"httpService\",\"type\":\"http-endpoint\",\"status\":\"UP\"," +
            "\"location\":\"http://0.0.0.0:" + port + "/rest\"}]}");
        assertRestByClient(context, HttpMethod.GET, "/gw/index", 200, expected);
    }

}
