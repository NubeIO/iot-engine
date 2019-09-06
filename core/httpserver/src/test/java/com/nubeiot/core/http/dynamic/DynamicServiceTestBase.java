package com.nubeiot.core.http.dynamic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerTestBase;
import com.nubeiot.core.http.dynamic.mock.GatewayServer;

public class DynamicServiceTestBase extends HttpServerTestBase {

    protected void startGatewayAndService(TestContext context, ContainerVerticle service,
                                          DeploymentOptions serviceOptions) {
        CountDownLatch latch = new CountDownLatch(1);
        DeploymentOptions config = new DeploymentOptions().setConfig(deployConfig(httpConfig.getPort()));
        VertxHelper.deploy(vertx.getDelegate(), context, config, gateway().get(), id -> {
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
        //small delay for enable dynamic api
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            context.fail(e);
        }
        System.out.println("FINISHED AFTER: " + (System.nanoTime() - start) / 1e9);
    }

    protected JsonObject deployConfig(int port) {
        return new JsonObject().put("__app__", new JsonObject().put("__http__", new JsonObject().put("port", port)));
    }

    @SuppressWarnings("unchecked")
    protected <T extends ContainerVerticle> Supplier<T> gateway() {
        return () -> (T) new GatewayServer();
    }

}
