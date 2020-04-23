package com.nubeiot.edge.module.gateway;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.dynamic.DynamicServiceTestBase;
import com.nubeiot.edge.module.gateway.mock.ExternalHttpServer;
import com.nubeiot.edge.module.gateway.mock.MockGatewayForwarder;

import lombok.NonNull;

@RunWith(VertxUnitRunner.class)
public class DriverRegistrationTest extends DynamicServiceTestBase {

    private int httpServicePort;
    private String registration;

    @BeforeClass
    public static void beforeSuite() { TestHelper.setup(); }

    @Override
    protected void startGatewayAndService(TestContext context, ContainerVerticle service,
                                          DeploymentOptions serviceOptions) {
        CountDownLatch latch = new CountDownLatch(1);
        VertxHelper.deploy(vertx.getDelegate(), context, new DeploymentOptions(), new MockGatewayForwarder(),
                           deployId -> latch.countDown());
        try {
            latch.await(TestHelper.TEST_TIMEOUT_SEC, TimeUnit.SECONDS);
            super.startGatewayAndService(context, service, serviceOptions);
            create(context, "xyz", httpServicePort, successAsserter(context, "xyz", id -> registration = id));
        } catch (InterruptedException e) {
            context.fail(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T extends ContainerVerticle> Supplier<T> gateway() {
        return () -> (T) new EdgeGatewayVerticle();
    }

    @Override
    protected DeploymentOptions getServiceOptions() throws IOException {
        httpServicePort = TestHelper.getRandomPort();
        return new DeploymentOptions().setConfig(deployConfig(httpServicePort));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T extends ContainerVerticle> T service() {
        return (T) new ExternalHttpServer();
    }

    @Test
    public void test_registerDriver_success(TestContext context) {
        create(context, "abc", 1234, successAsserter(context, "abc", null));
    }

    @Test
    public void test_registerDriver_serviceName_invalid(TestContext context) {
        create(context, null, 1234, resp -> context.assertEquals(HttpResponseStatus.BAD_REQUEST, resp.getStatus()));
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
            context.assertEquals(HttpResponseStatus.CREATED, resp.getStatus());
            context.assertEquals(serviceName, resp.body().getString("name"));
            context.assertNotNull(resp.body().getString("registration"));
            if (Objects.nonNull(register)) {
                register.accept(resp.body().getString("registration"));
            }
        };
    }

    private void create(TestContext context, String serviceName, int port, @NonNull Consumer<ResponseData> asserter) {
        JsonObject data = new HttpLocation().setHost(DEFAULT_HOST)
                                            .setPort(port)
                                            .setRoot("/gpio")
                                            .toJson()
                                            .put("name", serviceName);
        restRequest(context, HttpMethod.POST, "/gw/register", RequestData.builder().body(data).build()).subscribe(
            asserter::accept, context::fail);
    }

    @Test
    public void test_getRecords(TestContext context) {
        restRequest(context, HttpMethod.GET, "/gw/index", RequestData.builder().build()).subscribe(resp -> {
            context.assertEquals(200, resp.getStatus().code());
            System.out.println(resp.body());
            context.assertEquals(resp.body().getJsonArray("apis").size(), 1);
        }, context::fail);
    }

    @Test
    public void test_call_service_fromGateway(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/gpio/test", RequestData.builder().build(), 200,
                           new JsonObject().put("hello", "test"));
    }

    @Test
    public void test_post_value_on_service_fromGateway(TestContext context) {
        JsonObject body = new JsonObject().put("hello", "test");
        assertRestByClient(context, HttpMethod.POST, "/api/gpio/test", RequestData.builder().body(body).build(), 201,
                           body);
    }

    @Test
    public void test_unregisterDriver_notFound(TestContext context) {
        restRequest(context, HttpMethod.DELETE, "/gw/register/d", RequestData.builder().build()).subscribe(
            resp -> context.assertEquals(410, resp.getStatus().code()));
    }

    @Test
    public void test_unregisterDriver_success(TestContext context) {
        restRequest(context, HttpMethod.DELETE, "/gw/register/" + registration,
                    RequestData.builder().body(new JsonObject().put("port", httpServicePort)).build()).subscribe(
            resp -> context.assertEquals(204, resp.getStatus().code()), context::fail);
    }

    @Test
    public void test_register_alreadyExist(TestContext context) {
        JsonObject body = new HttpLocation().setHost(DEFAULT_HOST)
                                            .setPort(httpServicePort)
                                            .setRoot("/gpio")
                                            .toJson()
                                            .put("name", "xyz");
        restRequest(context, HttpMethod.POST, "/gw/register", RequestData.builder().body(body).build()).subscribe(
            resp -> {
                context.assertEquals(422, resp.getStatus().code());
                context.assertEquals(ErrorCode.ALREADY_EXIST.name(), resp.body().getString("code"));
                this.registration = resp.body().getString("registration");
            });
    }

}
