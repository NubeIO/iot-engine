package com.nubeiot.edge.module.scheduler;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;

@RunWith(VertxUnitRunner.class)
public class EdgeSchedulerDeletionTest extends EdgeSchedulerVerticleTest {

    @Test
    public void test_delete_existed_job_by_existed_trigger(TestContext context) {
        assertRestByClient(context, HttpMethod.DELETE, "/api/s/trigger/1/job/1", RequestData.builder().build(), 204,
                           new JsonObject());
    }

    @Test
    public void test_delete_existed_job_by_non_existed_trigger(TestContext context) {
        JsonObject expected = new JsonObject().put("message", "Not found resource with trigger_id=4 and job_id=1")
                                              .put("code", ErrorCode.NOT_FOUND);
        assertRestByClient(context, HttpMethod.DELETE, "/api/s/trigger/4/job/1", RequestData.builder().build(), 410,
                           expected);
    }

    @Test
    public void test_delete_non_existed_job_by_existed_trigger(TestContext context) {
        JsonObject expected = new JsonObject().put("message", "Not found resource with trigger_id=1 and job_id=3")
                                              .put("code", ErrorCode.NOT_FOUND);
        assertRestByClient(context, HttpMethod.DELETE, "/api/s/trigger/1/job/3", RequestData.builder().build(), 410,
                           expected);
    }

    @Test
    public void test_delete_non_existed_job_by_non_existed_trigger(TestContext context) {
        JsonObject expected = new JsonObject().put("message", "Not found resource with trigger_id=4 and job_id=5")
                                              .put("code", ErrorCode.NOT_FOUND);
        assertRestByClient(context, HttpMethod.DELETE, "/api/s/trigger/4/job/5", RequestData.builder().build(), 410,
                           expected);
    }

    @Test
    public void test_delete_existed_job_with_reference(TestContext context) {
        JsonObject expected = new JsonObject().put("message", "Resource with job_id=1 is using by another resource")
                                              .put("code", ErrorCode.BEING_USED);
        assertRestByClient(context, HttpMethod.DELETE, "/api/s/job/1", RequestData.builder().build(), 422, expected);
    }

    @Test
    public void test_delete_existed_job_without_reference(TestContext context) {
        assertRestByClient(context, HttpMethod.DELETE, "/api/s/job/2", RequestData.builder().build(), 204,
                           new JsonObject());
    }

}
