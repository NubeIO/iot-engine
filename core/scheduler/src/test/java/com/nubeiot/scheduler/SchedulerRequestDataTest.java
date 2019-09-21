package com.nubeiot.scheduler;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.scheduler.MockEventScheduler.MockJobModel;
import com.nubeiot.scheduler.job.JobModel;
import com.nubeiot.scheduler.trigger.CronTriggerModel;
import com.nubeiot.scheduler.trigger.PeriodicTriggerModel;
import com.nubeiot.scheduler.trigger.TriggerModel;

public class SchedulerRequestDataTest {

    @Test
    public void test_cron_serialize() throws JSONException {
        final JobModel j1 = MockJobModel.create("abc");
        final TriggerModel t1 = CronTriggerModel.builder().name("t1").expr("0 0/1 * 1/1 * ? *").build();
        JsonHelper.assertJson(new JsonObject("{\"job\":{\"type\":\"EVENT_JOB\",\"name\":\"abc\"," +
                                             "\"group\":\"DEFAULT\",\"process\":{\"address\":\"event.job.model" +
                                             ".test\",\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"}," +
                                             "\"callback\":{\"address\":\"event.job.model.callback.test\"," +
                                             "\"pattern\":\"PUBLISH_SUBSCRIBE\",\"action\":\"PUBLISH\"}," +
                                             "\"forwardIfFailure\":true},\"trigger\":{\"type\":\"CRON\"," +
                                             "\"name\":\"t1\",\"group\":\"DEFAULT\",\"timezone\":\"GMT\"," +
                                             "\"expression\":\"0 0/1 * 1/1 * ? *\"}}"),
                              SchedulerRequestData.create(j1, t1).toJson());
    }

    @Test
    public void test_cron_deserialize() {
        final JobModel j1 = MockJobModel.create("abc");
        final TriggerModel t1 = CronTriggerModel.builder().name("t1").expr("0 0/1 * 1/1 * ? *").build();
        final SchedulerRequestData data = JsonData.from("{\"job\":{\"type\":\"EVENT_JOB\",\"name\":\"abc\"," +
                                                        "\"group\":\"DEFAULT\",\"process\":{\"address\":\"event.job" +
                                                        ".model" + ".test\",\"pattern\":\"REQUEST_RESPONSE\"," +
                                                        "\"action\":\"CREATE\"}," +
                                                        "\"callback\":{\"address\":\"event.job.model.callback.test\"," +
                                                        "\"pattern\":\"PUBLISH_SUBSCRIBE\",\"action\":\"PUBLISH\"}," +
                                                        "\"forwardIfFailure\":true},\"trigger\":{\"type\":\"CRON\"," +
                                                        "\"name\":\"t1\",\"group\":\"DEFAULT\",\"timezone\":\"GMT\"," +
                                                        "\"expression\":\"0 0/1 * 1/1 * ? *\"}}",
                                                        SchedulerRequestData.class);
        Assert.assertEquals(j1, data.getJob());
        Assert.assertEquals(t1, data.getTrigger());
        Assert.assertEquals(SchedulerRequestData.create(j1, t1), data);
    }

    @Test
    public void test_periodic_deserialize() {
        final JobModel j1 = MockJobModel.create("abc");
        final TriggerModel t1 = PeriodicTriggerModel.builder().name("tr2").intervalInSeconds(5).build();
        final SchedulerRequestData data = JsonData.from("{\"job\":{\"type\":\"EVENT_JOB\",\"name\":\"abc\"," +
                                                        "\"group\":\"DEFAULT\",\"process\":{\"address\":\"event.job" +
                                                        ".model" + ".test\",\"pattern\":\"REQUEST_RESPONSE\"," +
                                                        "\"action\":\"CREATE\"}," +
                                                        "\"callback\":{\"address\":\"event.job.model.callback.test\"," +
                                                        "\"pattern\":\"PUBLISH_SUBSCRIBE\",\"action\":\"PUBLISH\"}," +
                                                        "\"forwardIfFailure\":true},\"trigger\":{\"type\":\"PERIODIC" +
                                                        "\",\"name\":\"tr2\",\"group\":\"DEFAULT\"," +
                                                        "\"intervalInSeconds\":5,\"repeat\":0}}",
                                                        SchedulerRequestData.class);
        Assert.assertEquals(j1, data.getJob());
        Assert.assertEquals(t1, data.getTrigger());
        Assert.assertEquals(SchedulerRequestData.create(j1, t1), data);
    }

}
