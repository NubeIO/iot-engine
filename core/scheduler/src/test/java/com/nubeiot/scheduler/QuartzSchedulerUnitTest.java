package com.nubeiot.scheduler;

import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobKey;
import org.skyscreamer.jsonassert.Customization;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.TestHelper.VertxHelper;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.utils.DateTimes;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.scheduler.MockEventScheduler.MockJobModel;
import com.nubeiot.scheduler.MockEventScheduler.MockProcessEventSchedulerListener;
import com.nubeiot.scheduler.job.EventJobModel;
import com.nubeiot.scheduler.trigger.CronTriggerModel;
import com.nubeiot.scheduler.trigger.PeriodicTriggerModel;
import com.nubeiot.scheduler.trigger.TriggerModel;

@RunWith(VertxUnitRunner.class)
public class QuartzSchedulerUnitTest {

    private static final Customization SKIP_LOCAL_DATE = new Customization("data.first_fire_time.local",
                                                                           (o1, o2) -> true);
    private static final Customization SKIP_UTC_DATE = new Customization("data.first_fire_time.utc", (o1, o2) -> true);
    @Rule
    public Timeout timeout = Timeout.seconds(TestHelper.TEST_TIMEOUT_SEC);
    private Vertx vertx;
    private SchedulerConfig config;
    private EventController controller;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
    }

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        config = new SchedulerConfig("test");
        VertxHelper.deploy(vertx, context, new DeploymentOptions().setConfig(config.toJson()),
                           new QuartzSchedulerUnit(),
                           successId -> controller = SharedDataDelegate.getEventController(vertx,
                                                                                           QuartzSchedulerUnit.class.getName()));
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test(timeout = 70 * 1000)
    public void test_add_cron_schedule_success(TestContext context) {
        final Async async = context.async(3);
        controller.register(MockEventScheduler.PROCESS_EVENT, new MockProcessEventSchedulerListener());
        DeliveryEvent event = initRegisterEvent(MockJobModel.create("abc"), CronTriggerModel.builder()
                                                                                            .name("t1")
                                                                                            .expr("0 0/1 * 1/1 * ? *")
                                                                                            .build());
        controller.request(event, EventbusHelper.replyAsserter(context, registerAsserter(context, async, "t1", "abc")));
        EventbusHelper.assertReceivedData(vertx, async, MockEventScheduler.CALLBACK_EVENT.getAddress(),
                                          JsonHelper.asserter(context, async, countResp(0)));
    }

    @Test
    public void test_add_periodic_schedule_success(TestContext context) throws InterruptedException {
        final Async async = context.async(4);
        controller.register(MockEventScheduler.PROCESS_EVENT, new MockProcessEventSchedulerListener());
        DeliveryEvent event = initRegisterEvent(MockJobModel.create("xxx"), PeriodicTriggerModel.builder()
                                                                                                .name("t2")
                                                                                                .intervalInSeconds(3)
                                                                                                .repeat(1)
                                                                                                .build());
        controller.request(event, EventbusHelper.replyAsserter(context, registerAsserter(context, async, "t2", "xxx")));
        final String addr = MockEventScheduler.CALLBACK_EVENT.getAddress();
        CountDownLatch latch = new CountDownLatch(1);
        EventbusHelper.assertReceivedData(vertx, async, addr, o -> {
            latch.countDown();
            JsonHelper.asserter(context, async, countResp(0)).accept(o);
        });
        latch.await(3, TimeUnit.SECONDS);
        EventbusHelper.assertReceivedData(vertx, async, addr, JsonHelper.asserter(context, async, countResp(1)));
    }

    @Test
    public void test_add_same_job_should_failed(TestContext context) throws InterruptedException {
        final Async async = context.async(2);
        controller.register(MockEventScheduler.PROCESS_EVENT, new MockProcessEventSchedulerListener());
        CronTriggerModel cronTrigger = CronTriggerModel.builder().name("t1").expr("0 0/1 * 1/1 * ? *").build();
        PeriodicTriggerModel periodicTrigger = PeriodicTriggerModel.builder().name("t2").intervalInSeconds(3).build();
        DeliveryEvent event1 = initRegisterEvent(MockJobModel.create("abc"), cronTrigger);
        DeliveryEvent event2 = initRegisterEvent(MockJobModel.create("abc"), periodicTrigger);
        CountDownLatch latch = new CountDownLatch(1);
        controller.request(event1, e -> {
            EventbusHelper.replyAsserter(context, async, registerResponse("t1", "abc"), SKIP_LOCAL_DATE, SKIP_UTC_DATE)
                          .handle(e);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
        final JsonObject error = new JsonObject(
            "{\"status\":\"FAILED\",\"action\":\"CREATE\",\"error\":{\"code\":\"SERVICE_ERROR\"," +
            "\"message\":\"Cannot add trigger and job in scheduler | Cause: " +
            "Unable to store Job : 'DEFAULT.abc', because one already exists with this identification.\"}}");
        controller.request(event2, EventbusHelper.replyAsserter(context, async, error));
    }

    @Test
    public void test_add_job_to_multi_trigger_should_success(TestContext context) throws InterruptedException {
        final Async async = context.async(2);
        controller.register(MockEventScheduler.PROCESS_EVENT, new MockProcessEventSchedulerListener());
        PeriodicTriggerModel periodicTrigger = PeriodicTriggerModel.builder().name("tr2").intervalInSeconds(10).build();
        DeliveryEvent event1 = initRegisterEvent(MockJobModel.create("abc"), periodicTrigger);
        DeliveryEvent event2 = initRegisterEvent(MockJobModel.create("xxx"), periodicTrigger);
        CountDownLatch latch = new CountDownLatch(1);
        controller.request(event1, e -> {
            EventbusHelper.replyAsserter(context, async, registerResponse("tr2", "abc"), SKIP_LOCAL_DATE, SKIP_UTC_DATE)
                          .handle(e);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
        controller.request(event2,
                           EventbusHelper.replyAsserter(context, registerAsserter(context, async, "tr2", "xxx")));
    }

    @Test
    public void test_remove_should_success(TestContext context) throws InterruptedException {
        final Async async = context.async(2);
        controller.register(MockEventScheduler.PROCESS_EVENT, new MockProcessEventSchedulerListener());
        final DeliveryEvent removeEvent = initRemoveRegisterEvent(new JobKey("abc"));
        JsonObject r = new JsonObject("{\"status\":\"SUCCESS\",\"action\":\"REMOVE\",\"data\":{\"unschedule\":false}}");
        controller.request(removeEvent, EventbusHelper.replyAsserter(context, async, r));
        DeliveryEvent event = initRegisterEvent(MockJobModel.create("abc"), CronTriggerModel.builder().name("tr1")
                                                                                            .expr("0 0/1 * 1/1 * ? *")
                                                                                            .build());
        CountDownLatch latch = new CountDownLatch(1);
        controller.request(event, e -> {
            final JsonObject resp = registerResponse("tr1", "abc");
            EventbusHelper.replyAsserter(context, async, resp, SKIP_LOCAL_DATE, SKIP_UTC_DATE).handle(e);
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
        r = new JsonObject("{\"status\":\"SUCCESS\",\"action\":\"REMOVE\",\"data\":{\"unschedule\":true}}");
        controller.request(removeEvent, EventbusHelper.replyAsserter(context, async, r));
    }

    private DeliveryEvent initRegisterEvent(EventJobModel job, TriggerModel trigger) {
        return DeliveryEvent.builder()
                            .address(config.getRegisterAddress())
                            .pattern(EventPattern.REQUEST_RESPONSE)
                            .action(EventAction.CREATE)
                            .payload(new JsonObject().put("job", job.toJson()).put("trigger", trigger.toJson()))
                            .build();
    }

    private DeliveryEvent initRemoveRegisterEvent(JobKey jobKey) {
        final JsonObject payload = new JsonObject().put("job_group", jobKey.getGroup())
                                                   .put("job_name", jobKey.getName());
        return DeliveryEvent.builder()
                            .address(config.getRegisterAddress())
                            .pattern(EventPattern.REQUEST_RESPONSE)
                            .action(EventAction.REMOVE).payload(payload)
                            .build();
    }

    private Handler<JsonObject> registerAsserter(TestContext context, Async async, String triggerName, String jobName) {
        return body -> {
            JsonHelper.assertJson(context, async, registerResponse(triggerName, jobName), body, SKIP_LOCAL_DATE,
                                  SKIP_UTC_DATE);
            JsonObject fft = body.getJsonObject("data").getJsonObject("first_fire_time", null);
            context.assertNotNull(fft);
            context.assertTrue(Strings.isNotBlank(fft.getString("local")));
            context.assertTrue(Strings.isNotBlank(fft.getString("utc")));
            context.assertTrue(DateTimes.parseISO8601ToZone(fft.getString("utc")).getOffset().equals(ZoneOffset.UTC));
        };
    }

    private JsonObject registerResponse(String triggerName, String jobName) {
        return new JsonObject(
            "{\"status\":\"SUCCESS\",\"action\":\"CREATE\",\"data\":{\"trigger\":{\"group\":\"DEFAULT\"," +
            "\"name\":\"" + triggerName + "\"},\"job\":{\"group\":\"DEFAULT\",\"name\":\"" + jobName + "\"}}}");
    }

    private JsonObject countResp(int c) {
        return new JsonObject(
            "{\"status\":\"SUCCESS\",\"action\":\"PUBLISH\",\"prevAction\":\"CREATE\",\"data\":{\"count\":" + c + "}}");
    }

}
