package com.nubeiot.edge.connector.bacnet;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.ReadinessAsserter;

public abstract class BACnetWithoutGatewayTest extends BaseBACnetVerticleTest {

    protected ReadinessAsserter createReadinessHandler(TestContext context, Async async) {
        return new ReadinessAsserter(context, async, new JsonObject("{\"total\":0}"));
    }

    @Override
    protected void deployServices(TestContext context, BACnetConfig bacnetCfg, BACnetVerticle verticle) {
        final Async async = context.async(2);
        VertxHelper.deploy(vertx, context, createDeploymentOptions(bacnetCfg), verticle, event -> {
            busClient = verticle.getEventbusClient()
                                .register(bacnetCfg.getReadinessAddress(), createReadinessHandler(context, async));
            TestHelper.testComplete(async);
        });
    }

}
