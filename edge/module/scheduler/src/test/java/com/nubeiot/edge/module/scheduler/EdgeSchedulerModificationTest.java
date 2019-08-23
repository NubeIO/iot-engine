package com.nubeiot.edge.module.scheduler;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;

@RunWith(VertxUnitRunner.class)
public class EdgeSchedulerModificationTest extends EdgeSchedulerVerticleTest {

    @Test
    public void test_update_trigger(TestContext context) {
        final RequestData reqData = RequestData.builder().body(MockSchedulerEntityHandler.TRIGGER_2.toJson()).build();
        final JsonObject expected = new JsonObject().put("action", EventAction.UPDATE)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", new JsonObject(
                                                        "{\"id\":2,\"group\":\"group1\",\"name\":\"trigger2\"," +
                                                        "\"type\":\"CRON\",\"detail\":{\"expression\":\"1 1 1 ? * MON" +
                                                        " *\",\"timezone\":\"Australia/Sydney\"},\"thread\":\"1 1 1 ?" +
                                                        " * MON *::Australia/Sydney\"}"));
        assertRestByClient(context, HttpMethod.PUT, "/api/s/trigger/2", reqData, 200, expected);
    }

    @Test
    public void test_update_non_existed_trigger(TestContext context) {
        final RequestData reqData = RequestData.builder().body(MockSchedulerEntityHandler.TRIGGER_1.toJson()).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message", "Not found resource with trigger_id=5");
        assertRestByClient(context, HttpMethod.PUT, "/api/s/trigger/5", reqData, 410, expected);
    }

    @Test
    public void test_update_non_existed_trigger_by_existed_job(TestContext context) {
        final RequestData reqData = RequestData.builder().body(MockSchedulerEntityHandler.TRIGGER_1.toJson()).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message",
                                                         "Not found resource with job_id=1 and trigger_id=3");
        assertRestByClient(context, HttpMethod.PUT, "/api/s/job/1/trigger/3", reqData, 410, expected);
    }

    @Test
    public void test_update_existed_trigger_by_non_existed_job(TestContext context) {
        final RequestData reqData = RequestData.builder().body(MockSchedulerEntityHandler.TRIGGER_1.toJson()).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message",
                                                         "Not found resource with job_id=3 and trigger_id=1");
        assertRestByClient(context, HttpMethod.PUT, "/api/s/job/3/trigger/1", reqData, 410, expected);
    }

    @Test
    public void test_patch_non_existed_trigger(TestContext context) {
        final RequestData reqData = RequestData.builder().body(MockSchedulerEntityHandler.TRIGGER_1.toJson()).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                    .put("message", "Not found resource with trigger_id=5");
        assertRestByClient(context, HttpMethod.PATCH, "/api/s/trigger/5", reqData, 410, expected);
    }

    @Test
    public void test_patch_trigger(TestContext context) {
        final RequestData reqData = RequestData.builder().body(new JsonObject().put("intervalInSeconds", 60)).build();
        final JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource", new JsonObject(
                                                        "{\"id\":2,\"group\":\"group1\",\"name\":\"trigger3\"," +
                                                        "\"type\":\"PERIODIC\",\"detail\":{\"intervalInSeconds\":60," +
                                                        "\"repeat\":10}}"));
        assertRestByClient(context, HttpMethod.PATCH, "/api/s/trigger/2", reqData, 200, expected);
    }

    @Test
    public void test_patch_trigger_invalid_expr(TestContext context) {
        final JsonObject entity = MockSchedulerEntityHandler.TRIGGER_1.toJson().put("expression", "x");
        entity.remove("name");
        entity.remove("group");
        final RequestData reqData = RequestData.builder().body(entity).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "Cannot parse cron expression");
        assertRestByClient(context, HttpMethod.PATCH, "/api/s/trigger/1", reqData, 400, expected);
    }

    @Test
    public void test_patch_trigger_invalid_expr_by_job(TestContext context) {
        final JsonObject body = MockSchedulerEntityHandler.TRIGGER_1.toJson().put("expression", "x");
        body.remove("name");
        body.remove("group");
        final RequestData reqData = RequestData.builder().body(new JsonObject().put("trigger", body)).build();
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "Cannot parse cron expression");
        assertRestByClient(context, HttpMethod.PATCH, "/api/s/job/1/trigger/1", reqData, 400, expected);
    }

}
