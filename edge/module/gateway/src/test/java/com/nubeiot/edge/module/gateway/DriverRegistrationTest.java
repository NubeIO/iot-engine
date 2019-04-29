package com.nubeiot.edge.module.gateway;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.dynamic.DynamicServiceTestBase;
import com.nubeiot.edge.module.gateway.mock.ExternalHttpServer;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class DriverRegistrationTest extends DynamicServiceTestBase {

    private int httpServicePort;
    private String registration;

    @SuppressWarnings("unchecked")
    protected <T extends ContainerVerticle> Supplier<T> gateway() {
        return () -> (T) new EdgeGatewayVerticle();
    }

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        httpServicePort = TestHelper.getRandomPort();
        startGatewayAndService(context, new ExternalHttpServer(),
                               new DeploymentOptions().setConfig(deployConfig(httpServicePort)));
        create(context, "xyz", httpServicePort, successAsserter(context, "xyz", id -> registration = id));
    }

    @Test
    public void test_registerDriver_success(TestContext context) {
        create(context, "abc", 1234, successAsserter(context, "abc", null));
    }

    @Test
    public void test_registerDriver_port_invalid(TestContext context) {
        create(context, "abc", -10, resp -> {
            context.assertEquals(HttpResponseStatus.BAD_REQUEST, resp.getStatus());
            context.assertEquals(ErrorCode.INVALID_ARGUMENT.name(), resp.body().getString("code"));
            context.assertEquals("Port is not in range [1, 65535]", resp.body().getString("message"));
        });
    }

    private Consumer<ResponseData> successAsserter(TestContext context, String serviceName, Consumer<String> register) {
        return resp -> {
            context.assertEquals(HttpResponseStatus.OK, resp.getStatus());
            context.assertEquals(serviceName, resp.body().getString("name"));
            context.assertNotNull(resp.body().getString("registration"));
            if (Objects.nonNull(register)) {
                register.accept(resp.body().getString("registration"));
            }
        };
    }

    private void create(TestContext context, String serviceName, int port, @NonNull Consumer<ResponseData> asserter) {
        JsonObject data = new HttpLocation().setHost(DEFAULT_HOST).setPort(port).toJson().put("name", serviceName);
        restRequest(context, HttpMethod.POST, "/api/drivers/registration",
                    RequestData.builder().body(data).build()).subscribe(asserter::accept, context::fail);
    }

    @Test
    public void test_getRecords(TestContext context) {
        restRequest(context, HttpMethod.GET, "/api/drivers", RequestData.builder().build()).subscribe(resp -> {
            context.assertEquals(200, resp.getStatus().code());
            context.assertEquals(resp.body().getJsonArray("records").size(), 1);
        }, context::fail);
    }

    @Test
    public void test_call_service_fromGateway(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/drivers/gpio/test", RequestData.builder().build(), 200,
                           new JsonObject().put("hello", "test"));
    }

    @Test
    public void test_unregisterDriver_notFound(TestContext context) {
        restRequest(context, HttpMethod.DELETE, "/api/drivers/registration/d", RequestData.builder().build()).subscribe(
            resp -> context.assertEquals(410, resp.getStatus().code()));
    }

    @Test
    public void test_unregisterDriver_success(TestContext context) {
        restRequest(context, HttpMethod.DELETE, "/api/drivers/registration/" + registration,
                    RequestData.builder().body(new JsonObject().put("port", httpServicePort)).build()).subscribe(
            resp -> context.assertEquals(204, resp.getStatus().code()), context::fail);
    }

    @Test
    public void test_register_alreadyExist(TestContext context) {
        restRequest(context, HttpMethod.POST, "/api/drivers/registration",
                    RequestData.builder().body(new JsonObject().put("port", httpServicePort)).build()).subscribe(
            resp -> {
                context.assertEquals(409, resp.getStatus().code());
                context.assertEquals(ErrorCode.ALREADY_EXIST.name(), resp.body().getString("code"));
                this.registration = resp.body().getString("registration");
            });
    }

}
