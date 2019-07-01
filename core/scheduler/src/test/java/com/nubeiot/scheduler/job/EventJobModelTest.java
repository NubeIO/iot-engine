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
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.scheduler.JobModel;
import com.nubeiot.scheduler.JobModel.JobType;

public class EventJobModelTest {

    private static final EventJobModel JOB_MODEL = EventJobModel.builder()
                                                                .key(JobKey.jobKey("abc"))
                                                                .payload(EventMessage.initial(EventAction.CREATE))
                                                                .process(EventModel.builder()
                                                                                   .pattern(
                                                                                       EventPattern.REQUEST_RESPONSE)
                                                                                   .addEvents(EventAction.CREATE)
                                                                                   .local(true)
                                                                                   .address("event.job.model.test")
                                                                                   .build())
                                                                .callback(EventModel.builder()
                                                                                    .pattern(
                                                                                        EventPattern.PUBLISH_SUBSCRIBE)
                                                                                    .addEvents(EventAction.CREATE)
                                                                                    .local(true)
                                                                                    .address(
                                                                                        "event.job.model.callback.test")
                                                                                    .build())
                                                                .build();

    @Test
    public void test_serialize() throws JSONException {
        System.out.println(JOB_MODEL.toJson());
        JSONAssert.assertEquals("{\"type\":\"EVENT_JOB\",\"name\":\"abc\",\"group\":\"DEFAULT\"," +
                                "\"payload\":{\"status\":\"INITIAL\",\"action\":\"CREATE\"}," +
                                "\"process\":{\"address\":\"event.job.model.test\"," +
                                "\"pattern\":\"REQUEST_RESPONSE\",\"local\":true,\"events\":[\"CREATE\"]}," +
                                "\"callback\":{\"address\":\"event.job.model.callback.test\"," +
                                "\"pattern\":\"PUBLISH_SUBSCRIBE\",\"local\":true,\"events\":[\"CREATE\"]}}",
                                JOB_MODEL.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_to_job_detail() {
        final JobDetail jobDetail = JOB_MODEL.toJobDetail();
        Assert.assertEquals(JOB_MODEL.getKey(), jobDetail.getKey());
        Assert.assertEquals(EventJob.class, jobDetail.getJobClass());
        final Object object = jobDetail.getJobDataMap().get(JobModel.JOB_DATA_KEY);
        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof EventJobModel);
        Assert.assertEquals(EventAction.CREATE, ((EventJobModel) object).getPayload().getAction());
        Assert.assertEquals("event.job.model.test", ((EventJobModel) object).getProcess().getAddress());
        Assert.assertEquals(EventPattern.REQUEST_RESPONSE, ((EventJobModel) object).getProcess().getPattern());
    }

    @Test
    public void test_deserialize() {
        EventJobModel jobModel = JsonData.convert(new JsonObject(
            "{\"type\":\"EVENT_JOB\",\"name\":\"abc\",\"group\":\"DEFAULT\"," +
            "\"payload\":{\"status\":\"INITIAL\",\"action\":\"CREATE\"}," +
            "\"process\":{\"address\":\"event.job.model.test\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"local\":true,\"events\":[\"CREATE\"]}," +
            "\"callback\":{\"address\":\"event.job.model.callback.test\"," +
            "\"pattern\":\"PUBLISH_SUBSCRIBE\",\"local\":true,\"events\":[\"CREATE\"]}}"), EventJobModel.class);
        Assert.assertEquals("DEFAULT", jobModel.getKey().getGroup());
        Assert.assertEquals("abc", jobModel.getKey().getName());
        Assert.assertEquals(JobType.EVENT_JOB, jobModel.getType());
        Assert.assertNotNull(jobModel);
    }

}
