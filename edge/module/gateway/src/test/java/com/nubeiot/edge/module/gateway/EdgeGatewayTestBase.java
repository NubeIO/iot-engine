package com.nubeiot.edge.module.gateway;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.http.HttpServerTestBase;

class EdgeGatewayTestBase extends HttpServerTestBase {

    void startEdgeGateway(TestContext context) {
        CountDownLatch latch = new CountDownLatch(1);
        DeploymentOptions config = new DeploymentOptions().setConfig(overridePort(httpConfig.getPort()));
        VertxHelper.deploy(vertx.getDelegate(), context, config, new EdgeGatewayVerticle(), id -> {
            System.out.println("Gateway Deploy Id: " + id);
            latch.countDown();
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

    JsonObject overridePort(int port) {
        return new JsonObject().put("__app__", new JsonObject().put("__http__", new JsonObject().put("port", port)));
    }

}
