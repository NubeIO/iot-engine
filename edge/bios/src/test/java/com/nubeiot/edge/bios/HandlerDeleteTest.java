package com.nubeiot.edge.bios;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

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
import com.nubeiot.edge.bios.service.BiosApplicationService;
import com.nubeiot.edge.installer.InstallerVerticle;
import com.nubeiot.edge.installer.loader.ModuleType;
import com.nubeiot.edge.installer.model.tables.pojos.Application;

@Ignore
public class HandlerDeleteTest extends BaseInstallerVerticleTest {

    @Before
    public void before(TestContext context) {
        super.before(context);
        this.insertModule(context, new Application().setAppId(MODULE_ID)
                                                    .setServiceType(ModuleType.JAVA)
                                                    .setServiceName(SERVICE_NAME)
                                                    .setState(State.ENABLED)
                                                    .setVersion(VERSION)
                                                    .setSystemConfig(APP_SYSTEM_CONFIG)
                                                    .setAppConfig(APP_CONFIG)
                                                    .setModifiedAt(DateTimes.now()));
    }

    @Override
    protected InstallerVerticle initMockupVerticle(TestContext context) {
        return new MockBiosEdgeVerticle(DeploymentAsserter.init(vertx, context));
    }

    @Test
    public void test_delete_should_success(TestContext context) {
        JsonObject body = new JsonObject().put("service_id", MODULE_ID);
        Async async = context.async();
        installerVerticle.getEventbusClient()
                         .fire(DeliveryEvent.from(BiosApplicationService.class.getName(), EventPattern.REQUEST_RESPONSE,
                                                  EventAction.REMOVE,
                                                  RequestData.builder().body(body).build().toJson()),
                               EventbusHelper.replyAsserter(context, resp -> {
                                   System.out.println(resp);
                                   context.assertEquals(resp.getString("status"), Status.SUCCESS.name());
                                   TestHelper.testComplete(async);
                               }));
        CountDownLatch latch = new CountDownLatch(2);
        Async async2 = context.async(2);
        //Event module is deployed/updated successfully, we still have a gap for DB update.
        long timer = this.vertx.setPeriodic(1000, event -> {
            installerVerticle.getEntityHandler().applicationDao().findOneById(GROUP_ID).subscribe(result -> {
                Application application = result.orElse(null);
                if (Objects.nonNull(application) && application.getState() != State.PENDING) {
                    return;
                }
                context.assertNull(application);
                TestHelper.testComplete(async2);
                latch.countDown();
            }, error -> {
                latch.countDown();
                context.fail(error);
                TestHelper.testComplete(async2);
            });
            installerVerticle.getEntityHandler()
                             .transDao()
                             .findManyByAppId(Collections.singletonList(MODULE_ID))
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
