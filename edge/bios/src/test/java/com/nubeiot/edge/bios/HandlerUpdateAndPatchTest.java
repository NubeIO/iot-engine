package com.nubeiot.edge.bios;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.bios.loader.DeploymentAsserter;
import com.nubeiot.edge.bios.mock.MockBiosEdgeVerticle;
import com.nubeiot.edge.installer.InstallerVerticle;
import com.nubeiot.edge.installer.loader.ModuleType;
import com.nubeiot.edge.installer.model.tables.pojos.TblModule;

@Ignore
public class HandlerUpdateAndPatchTest extends BaseInstallerVerticleTest {

    @Before
    public void before(TestContext context) {
        super.before(context);
        this.insertModule(context, new TblModule().setServiceId(MODULE_ID)
                                                  .setServiceType(ModuleType.JAVA)
                                                  .setServiceName(SERVICE_NAME)
                                                  .setState(State.ENABLED)
                                                  .setVersion(VERSION)
                                                  .setAppConfig(APP_CONFIG)
                                                  .setSystemConfig(APP_SYSTEM_CONFIG)
                                                  .setModifiedAt(DateTimes.now()));
    }

    @Override
    protected InstallerVerticle initMockupVerticle(TestContext context) {
        return new MockBiosEdgeVerticle(DeploymentAsserter.init(vertx, context));
    }

    @Test
    public void test_update_with_happy_case_should_success(TestContext context) {
        JsonObject metadata = new JsonObject().put("state", State.ENABLED)
                                              .put("version", VERSION)
                                              .put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID).put("metadata", metadata);

        executeThenAssert(EventAction.UPDATE, context, body,
                          response -> context.assertEquals(response.getString("status"), Status.SUCCESS.name()));

        //testing the module state and transaction status after update
        testingDBUpdated(context, State.ENABLED, Status.SUCCESS, new JsonObject());
    }

    @Test
    public void test_update_module_not_available_should_failed(TestContext context) {
        String idNotExist = ARTIFACT_ID + "com.nubeiot.edge.module.notexist";
        JsonObject metadata = new JsonObject().put("version", VERSION).put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", ARTIFACT_ID + idNotExist).put("metadata", metadata);

        executeThenAssert(EventAction.PATCH, context, body, response -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("code"), ErrorCode.NOT_FOUND.name());
        });
    }

    @Test
    public void test_update_missing_version_should_failed(TestContext context) {
        JsonObject metadata = new JsonObject().put("state", State.DISABLED).put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID).put("metadata", metadata);

        executeThenAssert(EventAction.UPDATE, context, body, response -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("message"), "Service version is mandatory");
        });
    }

    @Test
    public void test_update_missing_state_should_failed(TestContext context) {
        JsonObject metadata = new JsonObject().put("version", VERSION).put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID).put("metadata", metadata);

        executeThenAssert(EventAction.UPDATE, context, body, response -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("message"), "State is required!");
        });
    }

    @Test
    public void test_update_missing_metadata_should_failed(TestContext context) {
        JsonObject metadata = new JsonObject().put("version", VERSION)
                                              .put("state", State.ENABLED)
                                              .put("group_id", GROUP_ID)
                                              .put("version", VERSION);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID).put("metadata", metadata);

        executeThenAssert(EventAction.UPDATE, context, body, response -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("message"), "Service name is mandatory");
        });
    }

    @Test
    public void test_patch_should_success(TestContext context) {
        JsonObject appConfig = new JsonObject(
            "{\"__kafka__\":{\"__client__\":{\"bootstrap.servers\":[\"localhost:9094\"]}}}");
        JsonObject metadata = new JsonObject().put("state", State.DISABLED).put("service_name", SERVICE_NAME);
        JsonObject body = new JsonObject().put("service_id", MODULE_ID)
                                          .put("metadata", metadata)
                                          .put("appConfig", appConfig);

        executeThenAssert(EventAction.PATCH, context, body, response -> {
            System.out.println(response);
            context.assertEquals(response.getString("status"), Status.SUCCESS.name());
        });

        JsonObject expect = new JsonObject("{\"__kafka__\":{\"__client__\":{\"bootstrap" +
                                           ".servers\":[\"localhost:9094\"]}},\"__sql__\":{\"dialect\":\"H2\"," +
                                           "\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:file:./bios-installer\"," +
                                           "\"minimumIdle\":1,\"maximumPoolSize\":2,\"connectionTimeout\":30000," +
                                           "\"idleTimeout\":180000,\"maxLifetime\":300000}}}");

        testingDBUpdated(context, State.DISABLED, Status.SUCCESS, expect);
    }

    @Test
    public void test_patch_module_not_available_should_failed(TestContext context) {
        JsonObject body = new JsonObject().put("service_id", "abc123");

        executeThenAssert(EventAction.PATCH, context, body, response -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("code"), ErrorCode.NOT_FOUND.name());
        });
    }

}
