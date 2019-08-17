package com.nubeiot.edge.module.scheduler;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.module.scheduler.utils.SchedulerConverter.TriggerConverter;
import com.nubeiot.iotdata.scheduler.model.tables.pojos.TriggerEntity;

@RunWith(VertxUnitRunner.class)
public class EdgeSchedulerModificationTest extends EdgeSchedulerVerticleTest {

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

}
