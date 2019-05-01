package com.nubeiot.edge.bios;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;

@RunWith(VertxUnitRunner.class)
public class HandlerUpdateAndPatchTest extends BaseEdgeVerticleTest {

    @BeforeClass
    public static void beforeSuite() {
        BaseEdgeVerticleTest.beforeSuite();
    }

    @Before
    public void before(TestContext context) {
        super.before(context);
        this.insertModule(context, new TblModule().setServiceId(MODULE_ID)
                                                  .setServiceType(ModuleType.JAVA)
                                                  .setServiceName(SERVICE_NAME)
                                                  .setState(State.ENABLED)
                                                  .setVersion(VERSION)
                                                  .setDeployConfig(new JsonObject())
                                                  .setModifiedAt(DateTimes.nowUTC()));
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosEdgeVerticle(this.getConsumer(context));
    }

    @Test
    public void test_update_with_happy_case_should_success(TestContext context) {
        JsonObject appConfig = new JsonObject().put("", "");
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("state", State.ENABLED)
                                              .put("version", VERSION)
                                              .put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID)
                                          .put("metadata", metadata)
                                          .put("appConfig", appConfig);

        executeThenAssert(EventAction.UPDATE, context, body, (response, async) -> {
            context.assertEquals(response.getString("status"), Status.SUCCESS.name());
            TestHelper.testComplete(async);
        });

        //testing the module state and transaction status after update
        testingDBUpdated(context, MODULE_ID, State.ENABLED, Status.SUCCESS);
    }

    @Test
    public void test_update_module_not_available_should_failed(TestContext context) {
        JsonObject appConfig = new JsonObject().put("", "");
        String groupIdNotExist = "com.nubeiot.edge.module.notexist";
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", groupIdNotExist)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("service_id", ARTIFACT_ID + groupIdNotExist)
                                          .put("metadata", metadata)
                                          .put("appConfig", appConfig);

        executeThenAssert(EventAction.UPDATE, context, body, (response, async) -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("code"), ErrorCode.NOT_FOUND.name());
            TestHelper.testComplete(async);
        });
    }

    @Test
    public void test_update_missing_version_should_failed(TestContext context) {
        JsonObject appConfig = new JsonObject().put("", "");
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("state", State.DISABLED)
                                              .put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID)
                                          .put("metadata", metadata)
                                          .put("appConfig", appConfig);

        executeThenAssert(EventAction.UPDATE, context, body, (response, async) -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("message"), "Version is required!");
            TestHelper.testComplete(async);
        });
    }

    @Test
    public void test_update_missing_state_should_failed(TestContext context) {
        JsonObject appConfig = new JsonObject().put("", "");
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION)
                                              .put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID)
                                          .put("metadata", metadata)
                                          .put("appConfig", appConfig);

        executeThenAssert(EventAction.UPDATE, context, body, (response, async) -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("message"), "State is required!");
            TestHelper.testComplete(async);
        });
    }

    @Test
    public void test_update_missing_app_config_should_failed(TestContext context) {
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION)
                                              .put("state", State.ENABLED)
                                              .put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID).put("metadata", metadata);

        executeThenAssert(EventAction.UPDATE, context, body, (response, async) -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("message"), "App config is required!");
            TestHelper.testComplete(async);
        });
    }

    @Test
    public void test_patch_should_success(TestContext context) {
        JsonObject appConfig = new JsonObject().put("", "");
        JsonObject metadata = new JsonObject().put("artifact_id", ARTIFACT_ID)
                                              .put("group_id", GROUP_ID)
                                              .put("state", State.DISABLED)
                                              .put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID)
                                          .put("metadata", metadata)
                                          .put("appConfig", appConfig);

        executeThenAssert(EventAction.PATCH, context, body, (response, async) -> {
            context.assertEquals(response.getString("status"), Status.SUCCESS.name());
            TestHelper.testComplete(async);
        });

        //testing the module state and transaction status after update
        testingDBUpdated(context, MODULE_ID, State.DISABLED, Status.SUCCESS);
    }

    @Test
    public void test_patch_module_not_available_should_failed(TestContext context) {
        JsonObject body = new JsonObject().put("service_id", "abc123");

        executeThenAssert(EventAction.PATCH, context, body, (response, async) -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            TestHelper.testComplete(async);
        });
    }

}
