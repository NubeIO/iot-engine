package com.nubeiot.core.http.dynamic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.skyscreamer.jsonassert.Customization;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.http.HttpServerTestBase;
import com.nubeiot.core.http.dynamic.mock.GatewayServer;

class DynamicServiceTestBase extends HttpServerTestBase {

    static final Customization IGNORE_URI = new Customization("message.uri", (o1, o2) -> false);

    void startGatewayAndService(TestContext context, ContainerVerticle service, DeploymentOptions serviceOptions) {
        CountDownLatch latch = new CountDownLatch(1);
        DeploymentOptions config = new DeploymentOptions().setConfig(overridePort(httpConfig.getPort()));
        VertxHelper.deploy(vertx.getDelegate(), context, config, new GatewayServer(), id -> {
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

}
