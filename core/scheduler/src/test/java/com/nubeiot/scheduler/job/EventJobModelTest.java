package com.nubeiot.scheduler.job;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.scheduler.MockEventScheduler.MockJobModel;
import com.nubeiot.scheduler.job.JobModel.JobType;

public class EventJobModelTest {

    @Test
    public void test_serialize() throws JSONException {
        final JobModel jobModel = MockJobModel.create("abc");
        System.out.println(jobModel.toJson());
        JSONAssert.assertEquals("{\"type\":\"EVENT_JOB\",\"name\":\"abc\",\"group\":\"DEFAULT\"," +
                                "\"process\":{\"address\":\"event.job.model.test\",\"pattern\":\"REQUEST_RESPONSE\"," +
                                "\"action\":\"CREATE\"},\"callback\":{\"address\":\"event.job.model.callback.test\"," +
                                "\"pattern\":\"PUBLISH_SUBSCRIBE\",\"action\":\"PUBLISH\"},\"forwardIfFailure\":true}",
                                jobModel.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_to_job_detail() {
        final JobDetail jobDetail = MockJobModel.create("xyz").toJobDetail();
        Assert.assertEquals(new JobKey("xyz"), jobDetail.getKey());
        Assert.assertEquals(EventJob.class, jobDetail.getJobClass());
        final Object object = jobDetail.getJobDataMap().get(JobModel.JOB_DATA_KEY);
        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof EventJobModel);
        Assert.assertEquals(EventAction.CREATE, ((EventJobModel) object).getProcess().getAction());
        Assert.assertEquals("event.job.model.test", ((EventJobModel) object).getProcess().getAddress());
        Assert.assertEquals(EventPattern.REQUEST_RESPONSE, ((EventJobModel) object).getProcess().getPattern());
    }

    @Test
    public void test_deserialize() {
        EventJobModel jobModel = JsonData.convert(new JsonObject(
            "{\"type\":\"EVENT_JOB\",\"name\":\"abc\",\"group\":\"DEFAULT\",\"process\":{\"address\":\"event.job" +
            ".model.test\",\"pattern\":\"REQUEST_RESPONSE\"," +
            "\"action\":\"CREATE\"},\"callback\":{\"address\":\"event.job" +
            ".model.callback.test\",\"pattern\":\"PUBLISH_SUBSCRIBE\",\"action\":\"PUBLISH\"}," +
            "\"forwardIfFailure\":false}"), EventJobModel.class);
        Assert.assertNotNull(jobModel);
        Assert.assertEquals("DEFAULT", jobModel.getKey().getGroup());
        Assert.assertEquals("abc", jobModel.getKey().getName());
        Assert.assertEquals(JobType.EVENT_JOB, jobModel.type());
        Assert.assertFalse(jobModel.forwardIfFailure());
    }

}
