package com.nubeiot.edge.bios;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;

@RunWith(VertxUnitRunner.class)
public class HandlerDeployFailedTest extends BaseEdgeVerticleTest {

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosEdgeVerticle(DeploymentAsserter.init(vertx, context), true);
    }

    @Test
    public void test_create_when_deploy_failed(TestContext context) {
        JsonObject appConfig = new JsonObject().put("", "");
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("state", State.ENABLED)
                                              .put("version", VERSION)
                                              .put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("metadata", metadata).put("appConfig", appConfig);
        executeThenAssert(EventAction.CREATE, context, body, response -> {
            //TODO add asserter
            System.out.println(response);
        });
        testingDBUpdated(context, State.DISABLED, Status.FAILED, appConfig);
    }

    @Test
    public void test_update_when_deploy_failed(TestContext context) {
        createService(context);

        JsonObject appConfig = new JsonObject().put("", "");
        JsonObject metadata = new JsonObject().put("state", State.ENABLED)
                                              .put("version", VERSION)
                                              .put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID)
                                          .put("metadata", metadata)
                                          .put("appConfig", appConfig);
        executeThenAssert(EventAction.UPDATE, context, body, response -> {
            //TODO
            System.out.println(response);
        });
        testingDBUpdated(context, State.DISABLED, Status.FAILED, appConfig);
    }

    @Test
    public void test_patch_when_deploy_failed(TestContext context) {
        createService(context);
        JsonObject metadata = new JsonObject().put("state", State.ENABLED).put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID).put("metadata", metadata);
        executeThenAssert(EventAction.PATCH, context, body, response -> {
            //TODO
            System.out.println(response);
        });
        testingDBUpdated(context, State.DISABLED, Status.FAILED, APP_CONFIG);
    }

    @Test
    public void test_delete_when_deploy_failed(TestContext context) {
        createService(context);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID);
        executeThenAssert(EventAction.REMOVE, context, body, response -> {
            //TODO
            System.out.println(response);
        });
        testingDBUpdated(context, State.DISABLED, Status.FAILED, APP_CONFIG);
    }

    private void createService(TestContext context) {
        this.insertModule(context, new TblModule().setServiceId(MODULE_ID)
                                                  .setServiceType(ModuleType.JAVA)
                                                  .setServiceName(SERVICE_NAME)
                                                  .setState(State.ENABLED)
                                                  .setVersion(VERSION)
                                                  .setAppConfig(APP_CONFIG)
                                                  .setSystemConfig(APP_SYSTEM_CONFIG)
                                                  .setModifiedAt(DateTimes.nowUTC()));
    }

}
