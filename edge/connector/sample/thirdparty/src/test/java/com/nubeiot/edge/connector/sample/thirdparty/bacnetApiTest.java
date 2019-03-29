package com.nubeiot.edge.connector.sample.thirdparty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.HttpServerTestBase;

@RunWith(VertxUnitRunner.class)
public class bacnetApiTest extends HttpServerTestBase {
    //public class bacnetApiTest {

    @BeforeClass
    public static void beforeSuite() { TestHelper.setup(); }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Before
    public void before(TestContext context) throws IOException {
        super.before(context);
        startGatewayAndService(context, new BACnetVerticleTest(), new DeploymentOptions());
    }

    void startGatewayAndService(TestContext context, ContainerVerticle service, DeploymentOptions serviceOptions) {
        CountDownLatch latch = new CountDownLatch(1);
        DeploymentOptions config = new DeploymentOptions().setConfig(overridePort(httpConfig.getPort()));
        VertxHelper.deploy(vertx.getDelegate(), context, config, new DriverApiVerticleTest(), id -> {
            System.out.println("Gateway Deploy Id: " + id);
            VertxHelper.deploy(vertx.getDelegate(), context, serviceOptions, service, d -> {
                System.out.println("Service Deploy Id: " + d);
                latch.countDown();
            });
        });
        long start = System.nanoTime();
        try {
            context.assertTrue(latch.await(TestHelper.TEST_TIMEOUT_SEC, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            context.fail("Failed to start Gateway and HTTP Service");
        }
        System.out.println("FINISHED AFTER: " + (System.nanoTime() - start) / 1e9);
    }

    JsonObject overridePort(int port) {
        return new JsonObject().put("__app__", new JsonObject().put("__http__", new JsonObject().put("port", port)));
    }

    @Test
    public void test_event_not_found(TestContext context) {
        assertRestByClient(context, HttpMethod.POST, "/api/driver/bacnet/devices", 409,
                           new JsonObject().put("code", ErrorCode.STATE_ERROR)
                                           .put("message", "Unsupported event CREATE"));
    }

    @Test
    public void test_get_list_success(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/driver/bacnet/devices", 200, new JsonObject());
    }

}
