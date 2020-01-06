package com.nubeiot.edge.module.datapoint.service;

import static com.nubeiot.core.sql.decorator.EntityTransformer.AUDIT_FIELDS;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

public class PointValueServiceTest extends BaseDataPointServiceTest {

    static DeliveryEvent createPointEvent(PointValueData pv, boolean hasAudit) {
        final JsonObject data = JsonPojo.from(pv).toJson().put("point_id", pv.getPoint().toString());
        final RequestData reqData = RequestData.builder()
                                               .body(data)
                                               .filter(hasAudit ? new JsonObject().put(Filters.AUDIT, true) : null)
                                               .build();
        return DeliveryEvent.builder()
                            .address(PointValueService.class.getName())
                            .action(EventAction.CREATE_OR_UPDATE)
                            .addPayload(reqData)
                            .build();
    }

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_dml_unsupported_event(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.STATE_ERROR)
                                              .put("message", "Unsupported event CREATE");
        JsonObject body = new JsonObject().put("point_id", PrimaryKey.P_BACNET_SWITCH.toString());
        body.remove("point");
        RequestData req = RequestData.builder().body(body).build();
        asserter(context, false, expected, PointValueService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_dql_unsupported_event(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.STATE_ERROR)
                                              .put("message", "Unsupported event GET_LIST");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, false, expected, PointValueService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_point_data(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"priority\":5,\"value\":10,\"point\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\",\"priority_values\":" +
            "{\"1\":null,\"2\":null,\"3\":null,\"4\":null,\"5\":10,\"6\":9,\"7\":null,\"8\":10,\"9\":null," +
            "\"10\":null,\"11\":null,\"12\":null,\"13\":null,\"14\":null,\"15\":null,\"16\":null,\"17\":null}}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, true, expected, PointValueService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_create_or_update_pv_but_point_not_found(TestContext context) {
        final UUID uuid = UUID.randomUUID();
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message", "Not found resource with point_id=" + uuid);
        JsonObject body = MockData.searchData(PrimaryKey.P_GPIO_HUMIDITY).toJson().put("point_id", uuid.toString());
        body.remove("point");
        RequestData req = RequestData.builder().body(body).build();
        asserter(context, false, expected, PointValueService.class.getName(), EventAction.CREATE_OR_UPDATE, req);
    }

    @Test
    public void test_create_or_update_pv_already_existed(TestContext context) {
        final PointValueData v = MockData.searchData(PrimaryKey.P_GPIO_HUMIDITY).setPriority(2).setValue(15.0);
        v.getPriorityValues().add(2, 15);
        final JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource",
                                                         JsonPojo.from(v).toJson(JsonData.MAPPER, AUDIT_FIELDS));
        final JsonObject body = new JsonObject().put("value", 15.0)
                                                .put("priority", 2)
                                                .put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString());
        RequestData req = RequestData.builder().body(body).build();
        asserter(context, true, expected, PointValueService.class.getName(), EventAction.CREATE_OR_UPDATE, req);
    }

    @Test
    public void test_create_or_update_pv_not_yet_existed(TestContext context) {
        DeliveryEvent event = createPointEvent(
            new PointValueData().setPoint(PrimaryKey.P_GPIO_TEMP).setPriority(5).setValue(24d), false);
        Async async = context.async(1);
        controller().fire(event, EventbusHelper.replyAsserter(context, body -> {
            JsonObject data = new JsonObject(
                "{\"point\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"value\":24,\"priority\":5," +
                "\"priority_values\":{\"1\":null,\"2\":null,\"3\":null,\"4\":null,\"5\":24,\"6\":null,\"7\":null," +
                "\"8\":null,\"9\":null,\"10\":null,\"11\":null,\"12\":null,\"13\":null,\"14\":null,\"15\":null," +
                "\"16\":null,\"17\":null}}");
            JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                  .put("status", Status.SUCCESS)
                                                  .put("resource", data);
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"));
        }));
    }

    @Test
    public void test_create_or_update_pv_with_null_value(TestContext context) {
        final PointValueData v = MockData.searchData(PrimaryKey.P_BACNET_TEMP).setPriority(9).setValue(27.5);
        v.getPriorityValues().add(3, null);
        final JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                                    .put("status", Status.SUCCESS)
                                                    .put("resource",
                                                         JsonPojo.from(v).toJson(JsonData.MAPPER, AUDIT_FIELDS));
        final JsonObject body = new JsonObject().put("value", (Double) null)
                                                .put("priority", 3)
                                                .put("point_id", PrimaryKey.P_BACNET_TEMP.toString());
        final RequestData req = RequestData.builder().body(body).build();
        asserter(context, true, expected, PointValueService.class.getName(), EventAction.CREATE_OR_UPDATE, req);
    }

    @Test
    public void test_create_or_update_pv_and_assert_audit(TestContext context) throws InterruptedException {
        DeliveryEvent event = createPointEvent(
            new PointValueData().setPoint(PrimaryKey.P_BACNET_SWITCH).setPriority(5).setValue(24d), true);
        CountDownLatch latch = new CountDownLatch(2);
        Async async = context.async(2);
        controller().fire(event, EventbusHelper.replyAsserter(context, body -> {
            latch.countDown();
            JsonObject data = new JsonObject(
                "{\"point\":\"" + PrimaryKey.P_BACNET_SWITCH + "\",\"value\":24,\"priority\":5," +
                "\"priority_values\":{\"1\":null,\"2\":null,\"3\":null,\"4\":null,\"5\":24,\"6\":null,\"7\":null," +
                "\"8\":null,\"9\":null,\"10\":null,\"11\":null,\"12\":null,\"13\":null,\"14\":null,\"15\":null," +
                "\"16\":null,\"17\":null},\"time_audit\":{\"created_by\":\"UNDEFINED\",\"revision\":1}," +
                "\"sync_audit\":{\"status\":\"INITIAL\",\"data\":{\"message\":\"Not yet synced new resource\"}}}");
            JsonObject expected = new JsonObject().put("action", EventAction.CREATE).put("status", Status.SUCCESS)
                                                  .put("resource", data);
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"), JSONCompareMode.LENIENT);
        }));
        latch.await(TestHelper.TEST_TIMEOUT_SEC / 3, TimeUnit.SECONDS);
        DeliveryEvent event2 = createPointEvent(
            new PointValueData().setPoint(PrimaryKey.P_BACNET_SWITCH).setPriority(9).setValue(null), true);
        controller().fire(event2, EventbusHelper.replyAsserter(context, body -> {
            latch.countDown();
            JsonObject data = new JsonObject(
                "{\"point\":\"" + PrimaryKey.P_BACNET_SWITCH + "\",\"value\":24,\"priority\":5," +
                "\"priority_values\":{\"1\":null,\"2\":null,\"3\":null,\"4\":null,\"5\":24,\"6\":null,\"7\":null," +
                "\"8\":null,\"9\":null,\"10\":null,\"11\":null,\"12\":null,\"13\":null,\"14\":null,\"15\":null," +
                "\"16\":null},\"time_audit\":{\"created_by\":\"UNDEFINED\",\"last_modified_by\":\"UNDEFINED\"," +
                "\"revision\":2},\"sync_audit\":{\"status\":\"INITIAL\",\"data\":{\"message\":\"Not yet synced " +
                "modified resource with record revision 2\"}}}");
            JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                                  .put("status", Status.SUCCESS)
                                                  .put("resource", data);
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"), JSONCompareMode.LENIENT);
        }));
        Thread.sleep(500);
        latch.await(TestHelper.TEST_TIMEOUT_SEC / 3, TimeUnit.SECONDS);
    }

}
