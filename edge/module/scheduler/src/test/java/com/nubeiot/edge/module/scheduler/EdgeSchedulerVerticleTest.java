package com.nubeiot.edge.module.scheduler;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.TestHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.ExpectedResponse;
import com.nubeiot.core.http.dynamic.DynamicServiceTestBase;
import com.nubeiot.core.sql.JsonPojo;
import com.nubeiot.core.sql.SqlConfig;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.JobConverter;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.TriggerConverter;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobEntity;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.JobTrigger;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;
import com.nubeiot.scheduler.job.JobType;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(VertxUnitRunner.class)
public class EdgeSchedulerVerticleTest extends DynamicServiceTestBase {

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("org.jooq")).setLevel(Level.DEBUG);
    }

    @Override
    protected DeploymentOptions getServiceOptions() {
        JsonObject sqlConfig = new JsonObject(
            "{\"__hikari__\":{\"jdbcUrl\":\"jdbc:h2:mem:dbh2mem-" + UUID.randomUUID().toString() + "\"}}");
        final JsonObject appConfig = new JsonObject().put(SqlConfig.NAME, sqlConfig);
        return new DeploymentOptions().setConfig(new JsonObject().put(AppConfig.NAME, appConfig));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected EdgeSchedulerVerticle service() {
        return new EdgeSchedulerVerticle(MockSchedulerEntityHandler.class);
    }

    @Test
    public void test_get_list_trigger(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"triggers\":[{\"id\":1,\"group\":\"group1\",\"name\":\"trigger1\",\"type\":\"CRON\"," +
            "\"detail\":{\"expression\":\"0 0 0 ? * SUN *\",\"timezone\":\"Australia/Sydney\"}," +
            "\"thread\":\"0 0 0 ? * SUN *::Australia/Sydney\"},{\"id\":2,\"group\":\"group1\"," +
            "\"name\":\"trigger3\",\"type\":\"PERIODIC\",\"detail\":{\"intervalInSeconds\":120,\"repeat\":10}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/trigger", 200, expected);
    }

    @Test
    public void test_get_trigger(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"id\":1,\"group\":\"group1\",\"name\":\"trigger1\",\"type\":\"CRON\"," +
            "\"detail\":{\"expression\":\"0 0 0 ? * SUN *\",\"timezone\":\"Australia/Sydney\"}," +
            "\"thread\":\"0 0 0 ? * SUN *::Australia/Sydney\"}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/trigger/1", 200, expected);
    }

    @Test
    public void test_get_list_job_by_not_found_trigger(TestContext context) {
        assertRestByClient(context, HttpMethod.GET, "/api/s/trigger/3/job", 200,
                           new JsonObject().put("jobs", new ArrayList<>()));
    }

    @Test
    public void test_get_list_job_by_trigger(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"jobs\":[{\"id\":1,\"enabled\":true,\"job\":{\"id\":1,\"group\":\"group1\",\"name\":\"job1\"," +
            "\"type\":\"EVENT_JOB\",\"forward_if_failure\":true,\"detail\":{\"process\":{\"address\":\"scheduler.1\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"}}}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/trigger/1/job", 200, expected);
    }

    @Test
    public void test_get_job_by_trigger(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"id\":1,\"enabled\":true,\"job\":{\"id\":1,\"group\":\"group1\",\"name\":\"job1\"," +
            "\"type\":\"EVENT_JOB\",\"forward_if_failure\":true,\"detail\":{\"process\":{\"address\":\"scheduler.1\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"}}}}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/trigger/1/job/1", 200, expected);
    }

    @Test
    public void test_get_list_trigger_by_job(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"triggers\":[{\"id\":1,\"enabled\":true,\"trigger\":{\"id\":1,\"group\":\"group1\"," +
            "\"name\":\"trigger1\",\"type\":\"CRON\",\"detail\":{\"expression\":\"0 0 0 ? * SUN *\"," +
            "\"timezone\":\"Australia/Sydney\"},\"thread\":\"0 0 0 ? * SUN *::Australia/Sydney\"}}," +
            "{\"id\":2,\"enabled\":false,\"trigger\":{\"id\":2,\"group\":\"group1\",\"name\":\"trigger3\"," +
            "\"type\":\"PERIODIC\",\"detail\":{\"intervalInSeconds\":120,\"repeat\":10}}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/job/1/trigger", 200, expected);
    }

    @Test
    public void test_get_trigger_by_job(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"id\":2,\"trigger\":{\"id\":2,\"group\":\"group1\",\"name\":\"trigger3\",\"type\":\"PERIODIC\"," +
            "\"detail\":{\"intervalInSeconds\":120,\"repeat\":10}},\"enabled\":false}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/job/1/trigger/2", 200, expected);
    }

    @Test
    public void test_create_trigger(TestContext context) {
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
    public void test_patch_trigger_invalid_parse_expr(TestContext context) {
        final TriggerEntity entity = TriggerConverter.convert(MockSchedulerEntityHandler.TRIGGER_1);
        final JsonObject body = entity.setDetail(new JsonObject().put("expression", "x")).toJson();
        body.remove("name");
        body.remove("group");
        final RequestData reqData = RequestData.builder().body(body).build();
        final JsonObject expected = new JsonObject(
            "{\"message\":\"Cannot parse cron expression\",\"code\":\"INVALID_ARGUMENT\"}");
        assertRestByClient(context, HttpMethod.PATCH, "/api/s/trigger/1", reqData, 400, expected);
    }

    @Test
    public void test_get_list_job(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"jobs\":[{\"id\":1,\"group\":\"group1\",\"name\":\"job1\",\"type\":\"EVENT_JOB\"," +
            "\"forward_if_failure\":true,\"detail\":{\"process\":{\"address\":\"scheduler.1\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"}}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/job", 200, expected);
    }

    @Test
    public void test_create_job_success(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"action\":\"CREATE\",\"resource\":{\"id\":2,\"group\":\"group1\",\"name\":\"job2\"," +
            "\"type\":\"EVENT_JOB\",\"forward_if_failure\":true,\"detail\":{\"process\":{\"address\":\"scheduler.1\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"},\"callback\":{\"address\":\"scheduler.2\"," +
            "\"pattern\":\"POINT_2_POINT\",\"action\":\"PUBLISH\"}}},\"status\":\"SUCCESS\"}");
        create_job(context, JobConverter.convert(MockSchedulerEntityHandler.JOB_2),
                   ExpectedResponse.builder().expected(expected).statusCode(201).build());
    }

    @Test
    public void test_create_job_failed(TestContext context) {
        final JobEntity job = JobConverter.convert(MockSchedulerEntityHandler.JOB_2).setName("unknown").setDetail(null);
        final RequestData reqData = RequestData.builder().body(job.toJson()).build();
        final JsonObject expected = new JsonObject(
            "{\"message\":\"Job detail cannot be null\"," + "\"code\":\"INVALID_ARGUMENT\"}");
        assertRestByClient(context, HttpMethod.POST, "/api/s/job", reqData,
                           ExpectedResponse.builder().expected(expected).statusCode(400).build());
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
                           ExpectedResponse.builder().expected(expected).statusCode(400).build());
    }

    @Test
    public void test_create_job_then_assign_to_trigger(TestContext context) {
        Consumer<ResponseData> after = r -> {
            final Integer jobId = r.body().getJsonObject("resource").getInteger("id");
            JobTrigger composite = new JobTrigger().setEnabled(true).setJobId(jobId);
            final RequestData reqData = RequestData.builder().body(JsonPojo.from(composite).toJson()).build();
            final JsonObject expected = new JsonObject(
                "{\"message\":\"Not yet supported job type: XXX\",\"code\":\"INVALID_ARGUMENT\"}");
            assertRestByClient(context, HttpMethod.POST, "/api/s/trigger/1/job", reqData, 201, expected);
        };
        final JsonObject expected = new JsonObject(
            "{\"action\":\"CREATE\",\"resource\":{\"id\":2,\"group\":\"group1\",\"name\":\"job2\"," +
            "\"type\":\"EVENT_JOB\",\"forward_if_failure\":true,\"detail\":{\"process\":{\"address\":\"scheduler.1\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"},\"callback\":{\"address\":\"scheduler.2\"," +
            "\"pattern\":\"POINT_2_POINT\",\"action\":\"PUBLISH\"}}},\"status\":\"SUCCESS\"}");
        create_job(context, JobConverter.convert(MockSchedulerEntityHandler.JOB_2),
                   ExpectedResponse.builder().expected(expected).statusCode(201).after(after).build());
    }

    private void create_job(TestContext context, JobEntity job, ExpectedResponse expected) {
        final RequestData reqData = RequestData.builder().body(job.toJson()).build();
        assertRestByClient(context, HttpMethod.POST, "/api/s/job", reqData, expected);
    }

}
