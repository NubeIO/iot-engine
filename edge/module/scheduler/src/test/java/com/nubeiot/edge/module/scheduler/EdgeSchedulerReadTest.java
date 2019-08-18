package com.nubeiot.edge.module.scheduler;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class EdgeSchedulerReadTest extends EdgeSchedulerVerticleTest {

    @Test
    public void test_get_list_job(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"jobs\":[{\"id\":1,\"group\":\"group1\",\"name\":\"job1\",\"type\":\"EVENT_JOB\"," +
            "\"forward_if_failure\":true,\"detail\":{\"process\":{\"address\":\"scheduler.1\"," +
            "\"pattern\":\"REQUEST_RESPONSE\",\"action\":\"CREATE\"}}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/job", 200, expected);
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
    public void test_get_list_trigger_by_job_and_enable(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"triggers\":[{\"id\":1,\"enabled\":true,\"trigger\":{\"id\":1,\"group\":\"group1\"," +
            "\"name\":\"trigger1\",\"type\":\"CRON\",\"detail\":{\"expression\":\"0 0 0 ? * SUN *\"," +
            "\"timezone\":\"Australia/Sydney\"},\"thread\":\"0 0 0 ? * SUN *::Australia/Sydney\"}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/job/1/trigger?enabled=true", 200, expected);
    }

    @Test
    public void test_get_list_trigger_by_job_and_by_trigger_type(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"triggers\":[{\"id\":2,\"enabled\":false,\"trigger\":{\"id\":2,\"group\":\"group1\"," +
            "\"name\":\"trigger3\",\"type\":\"PERIODIC\",\"detail\":{\"intervalInSeconds\":120,\"repeat\":10}}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/job/1/trigger?trigger.type=PERIODIC", 200, expected);
    }

    @Test
    public void test_get_trigger_by_job(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"id\":2,\"trigger\":{\"id\":2,\"group\":\"group1\",\"name\":\"trigger3\",\"type\":\"PERIODIC\"," +
            "\"detail\":{\"intervalInSeconds\":120,\"repeat\":10}},\"enabled\":false}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/job/1/trigger/2", 200, expected);
    }

}
