package com.nubeiot.edge.bios;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.edge.bios.loader.DeploymentAsserter;
import com.nubeiot.edge.bios.service.BiosModuleService;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.loader.ModuleType;
import com.nubeiot.edge.core.model.tables.pojos.TblModule;

@RunWith(VertxUnitRunner.class)
public class HandlerDeleteTest extends BaseEdgeVerticleTest {

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
                                                  .setModifiedAt(DateTimes.now()));
    }

    @Override
    protected EdgeVerticle initMockupVerticle(TestContext context) {
        return new MockBiosEdgeVerticle(DeploymentAsserter.init(vertx, context));
    }

    @Test
    public void test_delete_should_success(TestContext context) {
        JsonObject body = new JsonObject().put("service_id", MODULE_ID);
        Async async = context.async();
        edgeVerticle.getEventController()
                    .request(DeliveryEvent.from(BiosModuleService.class.getName(), EventPattern.REQUEST_RESPONSE,
                                                EventAction.REMOVE, RequestData.builder().body(body).build().toJson()),
                             EventbusHelper.replyAsserter(context, resp -> {
                                 System.out.println(resp);
                                 context.assertEquals(resp.getString("status"), Status.SUCCESS.name());
                                 TestHelper.testComplete(async);
                             }));
        CountDownLatch latch = new CountDownLatch(2);
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
                latch.countDown();
            }, error -> {
                latch.countDown();
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
                                latch.countDown();
                            }
                        }, error -> {
                            latch.countDown();
                            context.fail(error);
                            TestHelper.testComplete(async2);
                        });
        });
        stopTimer(context, latch, timer);
    }

    @Test
    public void test_delete_invalid_module_should_failed(TestContext context) {
        JsonObject body = new JsonObject().put("service_id", "abc");
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message",
                                                   "Event REMOVE is not suitable in case of service is non-exist");
        Consumer<Object> asserter = JsonHelper.asserter(context, context.async(),
                                                        new JsonObject().put("status", Status.FAILED)
                                                                        .put("action", EventAction.REMOVE)
                                                                        .put("error", expected));
        executeThenAssert(EventAction.REMOVE, context, body, asserter::accept);
    }

}
