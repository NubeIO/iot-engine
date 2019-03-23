package com.nubeiot.edge.bios;

import java.util.Collections;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;

@RunWith(VertxUnitRunner.class)
public class HandlerUpdateAndPatchSuccessfullyTest extends BaseEdgeVerticleTest {

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
    public void test_update_success(TestContext context) {

        JsonObject deployConfig = new JsonObject().put("", "");
        EventMessage eventMessage = EventMessage.success(EventAction.UPDATE, RequestData.builder()
            .body(new JsonObject().put(
                "service_id",
                MODULE_ID)
                      .put(
                          "service_name",
                          SERVICE_NAME)
                      .put(
                          "version",
                          VERSION)
                      .put(
                          "deploy_config",
                          deployConfig)
                      .put(
                          "state",
                          State.ENABLED))
            .build());
        Async async = context.async();
        this.vertx.getDelegate()
            .eventBus()
            .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                  context.asyncAssertSuccess(handle -> {
                      JsonObject body = (JsonObject) handle.body();
                      context.assertEquals(body.getString("status"), Status.SUCCESS.name());
                      TestHelper.testComplete(async);
                  }));

        //testing the module state and transaction status after update
        testingDBUpdated(context, MODULE_ID, State.ENABLED, Status.SUCCESS);
    }

    @Test
    public void test_update_module_not_available(TestContext context) {

        EventMessage eventMessage = EventMessage.success(EventAction.UPDATE, RequestData.builder()
            .body(new JsonObject().put(
                "service_id", "abc123"))
            .build());
        Async async = context.async();
        this.vertx.getDelegate()
            .eventBus()
            .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                  context.asyncAssertSuccess(handle -> {
                      JsonObject body = (JsonObject) handle.body();
                      System.out.println(body);
                      context.assertEquals(body.getString("status"), Status.FAILED.name());
                      TestHelper.testComplete(async);
                  }));
    }

    @Test
    public void test_update_missing_version(TestContext context) {

        JsonObject deployConfig = new JsonObject().put("", "");
        EventMessage eventMessage = EventMessage.success(EventAction.UPDATE, RequestData.builder()
            .body(new JsonObject().put(
                "service_id",
                MODULE_ID)
                      .put(
                          "service_name",
                          SERVICE_NAME)
                      .put(
                          "deploy_config",
                          deployConfig)
                      .put(
                          "state",
                          State.DISABLED))
            .build());
        Async async = context.async();
        this.vertx.getDelegate()
            .eventBus()
            .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                  context.asyncAssertSuccess(handle -> {
                      JsonObject body = (JsonObject) handle.body();
                      context.assertEquals(body.getString("status"), Status.FAILED.name());
                      TestHelper.testComplete(async);
                  }));
    }

    @Test
    public void test_update_missing_state(TestContext context) {

        JsonObject deployConfig = new JsonObject().put("", "");
        EventMessage eventMessage = EventMessage.success(EventAction.UPDATE, RequestData.builder()
            .body(new JsonObject().put(
                "service_id",
                MODULE_ID)
                      .put(
                          "service_name",
                          SERVICE_NAME)
                      .put(
                          "version",
                          VERSION)
                      .put(
                          "deploy_config",
                          deployConfig))
            .build());
        Async async = context.async();
        this.vertx.getDelegate()
            .eventBus()
            .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                  context.asyncAssertSuccess(handle -> {
                      JsonObject body = (JsonObject) handle.body();
                      context.assertEquals(body.getString("status"), Status.FAILED.name());
                      TestHelper.testComplete(async);
                  }));
    }

    @Test
    public void test_update_missing_deploy_config(TestContext context) {

        JsonObject deployConfig = new JsonObject().put("", "");
        EventMessage eventMessage = EventMessage.success(EventAction.UPDATE, RequestData.builder()
            .body(new JsonObject().put(
                "service_id",
                MODULE_ID)
                      .put(
                          "service_name",
                          SERVICE_NAME)
                      .put(
                          "version",
                          VERSION)
                      .put(
                          "state",
                          State.ENABLED))
            .build());
        Async async = context.async();
        this.vertx.getDelegate()
            .eventBus()
            .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                  context.asyncAssertSuccess(handle -> {
                      JsonObject body = (JsonObject) handle.body();
                      context.assertEquals(body.getString("status"), Status.FAILED.name());
                      TestHelper.testComplete(async);
                  }));
    }

    @Test
    public void test_patch_success(TestContext context) {
        EventMessage eventMessage = EventMessage.success(EventAction.PATCH, RequestData.builder()
            .body(new JsonObject().put(
                "service_id",
                MODULE_ID)
                      .put(
                          "service_name",
                          SERVICE_NAME)
                      .put(
                          "state",
                          State.DISABLED))
            .build());
        Async async = context.async();
        this.vertx.getDelegate()
            .eventBus()
            .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                  context.asyncAssertSuccess(handle -> {
                      JsonObject body = (JsonObject) handle.body();
                      context.assertEquals(body.getString("status"), Status.SUCCESS.name());
                      TestHelper.testComplete(async);
                  }));

        //testing the module state and transaction status after update
        testingDBUpdated(context, MODULE_ID, State.DISABLED, Status.SUCCESS);
    }

    @Test
    public void test_patch_module_not_available(TestContext context) {

        EventMessage eventMessage = EventMessage.success(EventAction.PATCH, RequestData.builder()
            .body(new JsonObject().put(
                "service_id", "abc123"))
            .build());
        Async async = context.async();
        this.vertx.getDelegate()
            .eventBus()
            .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                  context.asyncAssertSuccess(handle -> {
                      JsonObject body = (JsonObject) handle.body();
                      context.assertEquals(body.getString("status"), Status.FAILED.name());
                      TestHelper.testComplete(async);
                  }));
    }

    @Test
    public void test_delete_successfully(TestContext context) {

        EventMessage eventMessage = EventMessage.success(EventAction.REMOVE, RequestData.builder()
            .body(new JsonObject().put(
                "service_id", MODULE_ID))
            .build());
        Async async = context.async();
        this.vertx.getDelegate()
            .eventBus()
            .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                  context.asyncAssertSuccess(handle -> {
                      JsonObject body = (JsonObject) handle.body();
                      context.assertEquals(body.getString("status"), Status.SUCCESS.name());
                      TestHelper.testComplete(async);
                  }));
        Async async2 = context.async(2);
        //Event module is deployed/updated successfully, we still have a gap for DB update.
        long timer = this.vertx.setPeriodic(1000, event -> {
            edgeVerticle.getEntityHandler()
                .getModuleDao()
                .findOneById(GROUP_ID)
                .subscribe(result -> {
                    TblModule tblModule = result.orElse(null);

                    if (Objects.nonNull(tblModule) && tblModule.getState() != State.PENDING) {
                        return;
                    }
                    context.assertNull(tblModule);
                    TestHelper.testComplete(async2);
                }, error -> {
                    context.fail(error);
                    TestHelper.testComplete(async2);
                });
            edgeVerticle.getEntityHandler()
                .getTransDao()
                .findManyByModuleId(Collections.singletonList(MODULE_ID))
                .subscribe(result -> {
                    if (Objects.nonNull(result) && !result.isEmpty() && result.get(0).getStatus() == Status.WIP) {
                        //do nothing
                    } else {
                        context.assertTrue(result.isEmpty());
                        TestHelper.testComplete(async2);
                    }
                }, error -> {
                    context.fail(error);
                    TestHelper.testComplete(async2);
                });
        });

        this.vertx.setTimer(20000, event -> {
            vertx.cancelTimer(timer);
            context.fail("Testing failed");
            TestHelper.testComplete(async);
            TestHelper.testComplete(async);
        });
    }

    @Test
    public void test_delete_invalid_module(TestContext context) {

        EventMessage eventMessage = EventMessage.success(EventAction.REMOVE, RequestData.builder()
            .body(new JsonObject().put(
                "service_id", "abc"))
            .build());
        Async async = context.async();
        this.vertx.getDelegate()
            .eventBus()
            .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                  context.asyncAssertSuccess(handle -> {
                      JsonObject body = (JsonObject) handle.body();
                      context.assertEquals(body.getString("status"), Status.FAILED.name());
                      TestHelper.testComplete(async);
                  }));
    }

}
