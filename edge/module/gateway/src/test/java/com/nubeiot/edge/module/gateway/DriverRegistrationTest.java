package com.nubeiot.edge.module.gateway;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.module.gateway.mock.HttpServer;

@RunWith(VertxUnitRunner.class)
public class DriverRegistrationTest extends EdgeGatewayTestBase {

    private int httpServicePort;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
    }

    @Before
    public void before(TestContext context) throws IOException {
        try {
            httpServicePort = TestHelper.getRandomPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.before(context);
        startEdgeGateway(context, new HttpServer(), new DeploymentOptions().setConfig(overridePort(httpServicePort)));
        // Registration
        assertRestByClient(context, HttpMethod.POST, "/api/drivers/registration",
                           RequestData.builder().body(new JsonObject().put("port", httpServicePort)).build(), 200,
                           new JsonObject());
    }

    @Test
    public void test_getRecords(TestContext context) {
        restRequest(context, HttpMethod.GET, "/api/drivers",
                    RequestData.builder().body(new JsonObject().put("port", httpServicePort)).build()).subscribe(
            resp -> {
                context.assertEquals(200, resp.getStatus().code());
                System.out.println("Response Body:: " + resp.body().encode());
                context.assertEquals(new JsonArray(resp.body().getString("records")).size(), 1);
            }, context::fail);
    }

    @Test
    public void test_driverRegistrationAndCallRegisteredApiFromGateway(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/api/test",
                           RequestData.builder().body(new JsonObject().put("port", httpServicePort)).build(), 200,
                           new JsonObject().put("hello", "test"));
    }

}
