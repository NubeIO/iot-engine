package com.nubeiot.core.http.rest;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.http.BaseHttpServerTest;
import com.nubeiot.core.http.HttpServerRouter;
import com.nubeiot.core.http.mock.MockApiDefinition;

@RunWith(VertxUnitRunner.class)
public class RestServerTest extends BaseHttpServerTest {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();
    @Rule
    public Timeout timeoutRule = Timeout.seconds(BaseHttpServerTest.TEST_TIMEOUT);

    @BeforeClass
    public static void beforeSuite() {
        BaseHttpServerTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Test
    public void test_not_yet_register(TestContext context) {
        startServer(context, new HttpServerRouter(), t -> context.assertTrue(t instanceof InitializerError));
    }

    @Test
    public void test_none_api_not_found(TestContext context) {
        String path = "/abc/";
        JsonObject expected = notFoundResponse(httpConfig.getPort(), path);
        startServer(context, new HttpServerRouter().registerApi(MockApiDefinition.MockAPI.class));
        assertRestByClient(context, HttpMethod.GET, path, 404, expected);
    }

    @Test
    public void test_api_not_found(TestContext context) {
        String path = "/api/xx";
        JsonObject expected = notFoundResponse(httpConfig.getPort(), path);
        startServer(context, new HttpServerRouter().registerApi(MockApiDefinition.MockAPI.class));
        assertRestByClient(context, HttpMethod.GET, path, 404, expected);
    }

    @Test
    public void test_api_throwable(TestContext context) {
        String path = "/api/test/error";
        JsonObject expected = ErrorMessage.parse(NubeException.ErrorCode.UNKNOWN_ERROR, "error").toJson();
        startServer(context, new HttpServerRouter().registerApi(MockApiDefinition.MockAPI.class));
        assertRestByClient(context, HttpMethod.GET, path, 500, expected);
    }

    @Test
    public void test_api_get_success(TestContext context) {
        String path = "/api/test";
        JsonObject expected = new JsonObject().put("abc", "xxx");
        startServer(context, new HttpServerRouter().registerApi(MockApiDefinition.MockAPI.class));
        assertRestByClient(context, HttpMethod.GET, path, 200, expected);
    }

}
