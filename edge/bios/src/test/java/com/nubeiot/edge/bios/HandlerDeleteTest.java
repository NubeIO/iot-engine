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
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;

@RunWith(VertxUnitRunner.class)
public class HandlerDeleteTest extends BaseEdgeVerticleTest {

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
                                                  .setSystemConfig(APP_SYSTEM_CONFIG)
                                                  .setAppConfig(APP_CONFIG)
                                                  .setModifiedAt(DateTimes.nowUTC()));
    }

    @After
    public void after(TestContext context) {
        super.after(context);
    }

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosEdgeVerticle(DeploymentAsserter.init(vertx, context));
    }

    @Test
    public void test_delete_should_success(TestContext context) {
        JsonObject body = new JsonObject().put("service_id", MODULE_ID);
        EventMessage eventMessage = EventMessage.success(EventAction.REMOVE, RequestData.builder().body(body).build());
        Async async = context.async();
        this.vertx.eventBus()
                  .send(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER.getAddress(), eventMessage.toJson(),
                        context.asyncAssertSuccess(handle -> {
                            JsonObject response = (JsonObject) handle.body();
                            context.assertEquals(response.getString("status"), Status.SUCCESS.name());
                            TestHelper.testComplete(async);
                        }));
        Async async2 = context.async(2);
        //Event module is deployed/updated successfully, we still have a gap for DB update.
        long timer = this.vertx.setPeriodic(1000, event -> {
            edgeVerticle.getEntityHandler().getModuleDao().findOneById(GROUP_ID).subscribe(result -> {
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
                            if (!Objects.nonNull(result) || result.isEmpty() ||
                                result.get(0).getStatus() != Status.WIP) {
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
        });
    }

    @Test
    public void test_delete_invalid_module_should_failed(TestContext context) {
        JsonObject body = new JsonObject().put("service_id", "abc");
        executeThenAssert(EventAction.REMOVE, context, body, (response, async) -> {
            context.assertEquals(response.getString("status"), Status.FAILED.name());
            context.assertEquals(response.getJsonObject("error").getString("code"), ErrorCode.NOT_FOUND.name());
            TestHelper.testComplete(async);
        });
    }

}
