package com.nubeiot.edge.module.gateway;

import java.io.IOException;

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
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.module.gateway.mock.ExternalHttpServer;

@RunWith(VertxUnitRunner.class)
public class DriverRegistrationTest extends EdgeGatewayTestBase {

    private int httpServicePort;
    private String registration;

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
        startEdgeGateway(context, new ExternalHttpServer(),
                         new DeploymentOptions().setConfig(overridePort(httpServicePort)));
        // Registration
        restRequest(context, HttpMethod.POST, "/api/drivers/registration",
                    RequestData.builder().body(new JsonObject().put("port", httpServicePort)).build()).subscribe(
            resp -> {
                context.assertEquals(200, resp.getStatus().code());
                this.registration = resp.body().getString("registration");
                System.out.println("Registration: " + resp.body().getString("registration"));
            });
    }

    @Test
    public void test_getRecords(TestContext context) {
        restRequest(context, HttpMethod.GET, "/api/drivers", RequestData.builder().build()).subscribe(resp -> {
            context.assertEquals(200, resp.getStatus().code());
            context.assertEquals(resp.body().getJsonArray("records").size(), 1);
        }, context::fail);
    }

    @Test
    public void test_driverRegistrationAndCallRegisteredApiFromGateway(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/api/test", RequestData.builder().build(), 200,
                           new JsonObject().put("hello", "test"));
    }

    @Test
    public void test_driverDeleteRegistrationNotFound(TestContext context) {
        restRequest(context, HttpMethod.DELETE, "/api/drivers/registration/d", RequestData.builder().build()).subscribe(
            resp -> context.assertEquals(404, resp.getStatus().code()));
    }

    @Test
    public void test_driverDeleteRegistration(TestContext context) {
        restRequest(context, HttpMethod.DELETE, "/api/drivers/registration/" + registration,
                    RequestData.builder().body(new JsonObject().put("port", httpServicePort)).build()).subscribe(
            resp -> context.assertEquals(204, resp.getStatus().code()));
    }

    @Test
    public void test_alreadyExistException(TestContext context) {
        restRequest(context, HttpMethod.POST, "/api/drivers/registration",
                    RequestData.builder().body(new JsonObject().put("port", httpServicePort)).build()).subscribe(
            resp -> {
                context.assertEquals(409, resp.getStatus().code());
                context.assertEquals("{\"error\":\"We have a service running on the given port.\"}",
                                     resp.body().encode());
                this.registration = resp.body().getString("registration");
            });
    }

}
