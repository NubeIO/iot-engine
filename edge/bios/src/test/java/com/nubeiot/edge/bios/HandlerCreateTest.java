package com.nubeiot.edge.bios;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.bios.loader.DeploymentAsserter;
import com.nubeiot.edge.core.EdgeVerticle;

@RunWith(VertxUnitRunner.class)
public class HandlerCreateTest extends BaseEdgeVerticleTest {

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosEdgeVerticle(DeploymentAsserter.init(vertx, context));
    }

    @Test
    public void test_create_should_success(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", APP_CONFIG);
        executeThenAssert(EventAction.CREATE, context, body, response -> {
            //TODO add asserter
            System.out.println(response);
        });
        //Checking module state and transaction status
        testingDBUpdated(context, State.ENABLED, Status.SUCCESS, APP_CONFIG);
    }

    @Test
    public void test_create_missing_app_config_should_failed(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("metadata", metadata);
        executeThenAssert(EventAction.CREATE, context, body, response -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("message"), "App config is required!");
        });
    }

}
