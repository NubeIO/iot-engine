package com.nubeiot.edge.module.scheduler;

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
import com.nubeiot.edge.module.scheduler.pojos.JobTriggerComposite;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;

@RunWith(VertxUnitRunner.class)
public class EdgeSchedulerCreationTest extends EdgeSchedulerVerticleTest {

    @Test
    public void test_create_trigger_success(TestContext context) {
        final JsonObject body = MockSchedulerEntityHandler.TRIGGER_2.toJson();
        System.out.println(body);
        final RequestData reqData = RequestData.builder().body(body).build();
        final JsonObject expected = new JsonObject(
            "{\"action\":\"CREATE\",\"resource\":{\"id\":3,\"group\":\"group1\",\"name\":\"trigger2\"," +
            "\"type\":\"CRON\",\"detail\":{\"expression\":\"1 1 1 ? * MON *\",\"timezone\":\"Australia/Sydney\"}," +
            "\"thread\":\"1 1 1 ? * MON *::Australia/Sydney\"},\"status\":\"SUCCESS\"}");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger", reqData, 201, expected);
    }

    @Test
    public void test_create_invalid_trigger(TestContext context) {
        final JsonObject body = MockSchedulerEntityHandler.TRIGGER_2.toJson().put("expression", (String) null);
        final RequestData reqData = RequestData.builder().body(body).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "Cannot parse cron expression");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger", reqData, 400, expected);
    }

    @Test
    public void test_create_job_success(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"action\":\"CREATE\",\"resource\":{\"id\":3,\"group\":\"group1\",\"name\":\"job2\"," +
            "\"type\":\"EVENT_JOB\",\"forward_if_failure\":true,\"detail\":{\"process\":{\"address\":\"scheduler.1\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"},\"callback\":{\"address\":\"scheduler.2\"," +
            "\"pattern\":\"POINT_2_POINT\",\"action\":\"PUBLISH\"}}},\"status\":\"SUCCESS\"}");
        createJob(context, MockSchedulerEntityHandler.JOB_2.toJson(),
                  ExpectedResponse.builder().expected(expected).code(201).build());
    }

    @Test
    public void test_create_invalid_job(TestContext context) {
        final JsonObject job = MockSchedulerEntityHandler.JOB_2.toJson()
                                                               .put("name", "unknown")
                                                               .put("process", (String) null);
        final RequestData reqData = RequestData.builder().body(job).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "Job detail cannot be null");
        assertRestByClient(context, HttpMethod.POST, "/api/s/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(400).build());
    }

    @Test
    public void test_create_job_unsupported(TestContext context) {
        final JsonObject job = MockSchedulerEntityHandler.JOB_2.toJson().put("name", "unknown").put("type", "XXX");
        final RequestData reqData = RequestData.builder().body(job).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "Not yet supported job type: XXX");
        assertRestByClient(context, HttpMethod.POST, "/api/s/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(400).build());
    }

    @Test
    public void test_assign_non_existed_job_to_existed_trigger(TestContext context) {
        final JobTrigger composite = new JobTrigger().setEnabled(true).setJobId(3);
        final RequestData reqData = RequestData.builder().body(JsonPojo.from(composite).toJson()).build();
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message", "Not found resource with job_id=3");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/1/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(410).build());
    }

    @Test
    public void test_assign_existed_job_to_non_existed_trigger(TestContext context) {
        final JobTrigger composite = new JobTrigger().setEnabled(true).setJobId(1);
        final RequestData reqData = RequestData.builder().body(JsonPojo.from(composite).toJson()).build();
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message", "Not found resource with trigger_id=3");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/3/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(410).build());
    }

    @Test
    public void test_assign_non_existed_job_to_non_existed_trigger(TestContext context) {
        final JobTrigger composite = new JobTrigger().setEnabled(true).setJobId(5);
        final RequestData reqData = RequestData.builder().body(JsonPojo.from(composite).toJson()).build();
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message", "Not found resource with trigger_id=5");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/5/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(410).build());
    }

    @Test
    public void test_assign_job_is_already_linked_to_trigger(TestContext context) {
        final JobTrigger composite = new JobTrigger().setEnabled(true).setJobId(1);
        final RequestData reqData = RequestData.builder().body(JsonPojo.from(composite).toJson()).build();
        JsonObject expect = new JsonObject().put("code", ErrorCode.ALREADY_EXIST)
                                            .put("message", "Already existed resource with trigger_id=1 and job_id=1");
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/1/job", reqData,
                           ExpectedResponse.builder().expected(expect).code(409).build());
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
            "{\"action\":\"CREATE\",\"resource\":{\"id\":3,\"group\":\"group1\",\"name\":\"job2\"," +
            "\"type\":\"EVENT_JOB\",\"forward_if_failure\":true,\"detail\":{\"process\":{\"address\":\"scheduler.1\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"},\"callback\":{\"address\":\"scheduler.2\"," +
            "\"pattern\":\"POINT_2_POINT\",\"action\":\"PUBLISH\"}}},\"status\":\"SUCCESS\"}");
        createJob(context, MockSchedulerEntityHandler.JOB_2.toJson(),
                  ExpectedResponse.builder().expected(expected).code(201).after(after).build());
    }

    @Test
    public void test_create_job_by_trigger(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"action\":\"CREATE\",\"resource\":{\"id\":3,\"enabled\":true},\"status\":\"SUCCESS\"}");
        final JsonObject job = MockSchedulerEntityHandler.JOB_2.toJson();
        final JsonObject body = JsonPojo.from(new JobTriggerComposite().setEnabled(true)).toJson().put("job", job);
        final RequestData reqData = RequestData.builder().body(body).build();
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/1/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(201).build());
    }

    @Test
    public void test_create_existed_job_by_trigger(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.ALREADY_EXIST)
                                                    .put("message", "Already existed resource with job_id=1");
        final JsonObject job = MockSchedulerEntityHandler.JOB_2.toJson().put("id", 1);
        final JsonObject body = JsonPojo.from(new JobTriggerComposite().setEnabled(true)).toJson().put("job", job);
        final RequestData reqData = RequestData.builder().body(body).build();
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/1/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(409).build());
    }

    @Test
    public void test_create_invalid_job_by_trigger(TestContext context) {
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "Job detail cannot be null");
        final JsonObject job = MockSchedulerEntityHandler.JOB_2.toJson()
                                                               .put("name", "unknown")
                                                               .put("process", (String) null);
        final JsonObject body = JsonPojo.from(new JobTriggerComposite().setEnabled(true)).toJson().put("job", job);
        final RequestData reqData = RequestData.builder().body(body).build();
        assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/1/job", reqData,
                           ExpectedResponse.builder().expected(expected).code(400).build());
    }

}
