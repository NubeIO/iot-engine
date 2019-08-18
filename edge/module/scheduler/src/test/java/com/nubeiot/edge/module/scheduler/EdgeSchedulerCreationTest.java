package com.nubeiot.edge.module.scheduler;

import java.util.Collections;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.ExpectedResponse;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.scheduler.service.JobTriggerComposite;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.JobConverter;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.TriggerConverter;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.scheduler.job.JobType;

@RunWith(VertxUnitRunner.class)
public class EdgeSchedulerCreationTest extends EdgeSchedulerVerticleTest {

    @Test
    public void test_create_trigger_success(TestContext context) {
        final TriggerEntity entity = TriggerConverter.convert(MockSchedulerEntityHandler.TRIGGER_2);
        final RequestData reqData = RequestData.builder().body(entity.toJson()).build();
        final JsonObject expected = new JsonObject(
            "{\"action\":\"CREATE\",\"resource\":{\"id\":3,\"group\":\"group1\",\"name\":\"trigger2\"," +
            "\"type\":\"CRON\",\"detail\":{\"expression\":\"0 0 1 ? * SUN *\",\"timezone\":\"Australia/Sydney\"}," +
            "\"thread\":\"0 0 1 ? * SUN *::Australia/Sydney\"},\"status\":\"SUCCESS\"}");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger", reqData, 201, expected);
    }

    @Test
    public void test_create_trigger_failed(TestContext context) {
        final TriggerEntity entity = TriggerConverter.convert(MockSchedulerEntityHandler.TRIGGER_2);
        final RequestData reqData = RequestData.builder().body(entity.setDetail(null).toJson()).build();
        final JsonObject expected = new JsonObject(
            "{\"message\":\"Trigger detail cannot be null\",\"code\":\"INVALID_ARGUMENT\"}");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger", reqData, 400, expected);
    }

    @Test
    public void test_create_job_success(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"action\":\"CREATE\",\"resource\":{\"id\":2,\"group\":\"group1\",\"name\":\"job2\"," +
            "\"type\":\"EVENT_JOB\",\"forward_if_failure\":true,\"detail\":{\"process\":{\"address\":\"scheduler.1\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"},\"callback\":{\"address\":\"scheduler.2\"," +
            "\"pattern\":\"POINT_2_POINT\",\"action\":\"PUBLISH\"}}},\"status\":\"SUCCESS\"}");
        createJob(context, JobConverter.convert(MockSchedulerEntityHandler.JOB_2),
                  ExpectedResponse.builder().expected(expected).code(201).build());
    }

    @Test
    public void test_create_job_failed(TestContext context) {
        final JobEntity job = JobConverter.convert(MockSchedulerEntityHandler.JOB_2).setName("unknown").setDetail(null);
        final RequestData reqData = RequestData.builder().body(job.toJson()).build();
        final JsonObject expected = new JsonObject(
            "{\"message\":\"Job detail cannot be null\"," + "\"code\":\"INVALID_ARGUMENT\"}");
        assertRestByClient(context, HttpMethod.POST, "/api/s/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(400).build());
    }

    @Test
    public void test_create_job_unsupported(TestContext context) {
        final JobEntity job = JobConverter.convert(MockSchedulerEntityHandler.JOB_2)
                                          .setName("unknown")
                                          .setType(JobType.factory("xxx"));
        final RequestData reqData = RequestData.builder().body(job.toJson()).build();
        final JsonObject expected = new JsonObject(
            "{\"message\":\"Not yet supported job type: XXX\",\"code\":\"INVALID_ARGUMENT\"}");
        assertRestByClient(context, HttpMethod.POST, "/api/s/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(400).build());
    }

    @Test
    public void test_assign_non_existed_job_to_existed_trigger(TestContext context) {
        final JobTrigger composite = new JobTrigger().setEnabled(true).setJobId(3);
        final RequestData reqData = RequestData.builder().body(JsonPojo.from(composite).toJson()).build();
        JsonObject expected = new JsonObject(
            "{\"message\":\"Not found resource with job_id=3\",\"code\":\"NOT_FOUND\"}");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/1/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(410).build());
    }

    @Test
    public void test_assign_existed_job_to_non_existed_trigger(TestContext context) {
        final JobTrigger composite = new JobTrigger().setEnabled(true).setJobId(1);
        final RequestData reqData = RequestData.builder().body(JsonPojo.from(composite).toJson()).build();
        JsonObject expected = new JsonObject(
            "{\"message\":\"Not found resource with trigger_id=3\",\"code\":\"NOT_FOUND\"}");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/3/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(404).build());
    }

    @Test
    public void test_assign_job_is_already_linked_to_trigger(TestContext context) {
        final JobTrigger composite = new JobTrigger().setEnabled(true).setJobId(1);
        final RequestData reqData = RequestData.builder().body(JsonPojo.from(composite).toJson()).build();
        JsonObject expected = new JsonObject(
            "{\"message\":\"Resource with job_id=1 is already referenced to resource with trigger_id=1\",\"code\":\"" +
            ErrorCode.ALREADY_EXIST + "\"}");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/1/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(409).build());
    }

    @Test
    public void test_create_job_then_assign_to_trigger(TestContext context) {
        final Consumer<ResponseData> after = r -> {
            final Integer jobId = r.body().getJsonObject("resource").getInteger("id");
            final JobTrigger composite = new JobTrigger().setEnabled(true).setJobId(jobId);
            final RequestData reqData = RequestData.builder().body(JsonPojo.from(composite).toJson()).build();
            JsonObject expected = new JsonObject(
                "{\"action\":\"CREATE\",\"resource\":{\"id\":3,\"enabled\":true},\"status\":\"SUCCESS\"}");
            assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/1/job", reqData,
                               ExpectedResponse.builder().expected(expected).code(201).build());
        };
        final JsonObject expected = new JsonObject(
            "{\"action\":\"CREATE\",\"resource\":{\"id\":2,\"group\":\"group1\",\"name\":\"job2\"," +
            "\"type\":\"EVENT_JOB\",\"forward_if_failure\":true,\"detail\":{\"process\":{\"address\":\"scheduler.1\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"},\"callback\":{\"address\":\"scheduler.2\"," +
            "\"pattern\":\"POINT_2_POINT\",\"action\":\"PUBLISH\"}}},\"status\":\"SUCCESS\"}");
        createJob(context, JobConverter.convert(MockSchedulerEntityHandler.JOB_2),
                  ExpectedResponse.builder().expected(expected).code(201).after(after).build());
    }

    @Test
    public void test_create_new_job_by_trigger(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"action\":\"CREATE\",\"resource\":{\"id\":3,\"enabled\":true},\"status\":\"SUCCESS\"}");
        final JobEntity job = JobConverter.convert(MockSchedulerEntityHandler.JOB_2);
        final JsonObject body = JsonPojo.from(
            new JobTriggerComposite().wrap(Collections.singletonMap("job", job)).setEnabled(true)).toJson();
        final RequestData reqData = RequestData.builder().body(body).build();
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/1/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(201).build());
    }

}
