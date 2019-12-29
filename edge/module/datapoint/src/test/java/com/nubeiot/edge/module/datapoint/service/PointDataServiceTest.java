package com.nubeiot.edge.module.datapoint.service;

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
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.RequestData.Filters;
import com.nubeiot.core.dto.Sort;
import com.nubeiot.core.dto.Sort.SortType;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

public class PointDataServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_create_point_data_but_point_not_found(TestContext context) {
        final UUID uuid = UUID.randomUUID();
        JsonObject expected = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                              .put("message", "Not found resource with point_id=" + uuid);
        JsonObject body = MockData.searchData(PrimaryKey.P_GPIO_HUMIDITY).toJson().put("point_id", uuid.toString());
        body.remove("point");
        RequestData req = RequestData.builder().body(body).build();
        asserter(context, false, expected, PointValueService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_create_point_data_already_existed(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.ALREADY_EXIST)
                                              .put("message", "Already existed resource with point_id=" +
                                                              PrimaryKey.P_GPIO_HUMIDITY);
        JsonObject body = MockData.searchData(PrimaryKey.P_GPIO_HUMIDITY)
                                  .toJson()
                                  .put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString());
        body.remove("point");
        RequestData req = RequestData.builder().body(body).build();
        asserter(context, false, expected, PointValueService.class.getName(), EventAction.CREATE, req);
    }

    @Test
    public void test_update_point_value_unsupported(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.STATE_ERROR)
                                              .put("message", "Unsupported event UPDATE");
        JsonObject body = new JsonObject().put("point_id", PrimaryKey.P_BACNET_SWITCH.toString());
        body.remove("point");
        RequestData req = RequestData.builder().body(body).build();
        asserter(context, false, expected, PointValueService.class.getName(), EventAction.UPDATE, req);
    }

    static DeliveryEvent createPointEvent(EventAction action, PointValueData pv, boolean hasAudit) {
        JsonObject data = JsonPojo.from(pv).toJson().put("point_id", pv.getPoint().toString());
        final RequestData reqData = RequestData.builder()
                                               .body(data)
                                               .filter(hasAudit ? new JsonObject().put(Filters.AUDIT, true) : null)
                                               .build();
        return DeliveryEvent.builder()
                            .address(PointValueService.class.getName())
                            .action(action)
                            .addPayload(reqData)
                            .build();
    }

    @Test
    public void test_create_point_data(TestContext context) {
        DeliveryEvent event = createPointEvent(EventAction.CREATE, new PointValueData().setPoint(PrimaryKey.P_GPIO_TEMP)
                                                                                       .setPriority(5)
                                                                                       .setValue(24d), false);
        Async async = context.async(1);
        controller().fire(event, EventbusHelper.replyAsserter(context, body -> {
            JsonObject data = new JsonObject(
                "{\"point\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"value\":24,\"priority\":5," +
                "\"priority_values\":{\"1\":null,\"2\":null,\"3\":null,\"4\":null,\"5\":24,\"6\":null,\"7\":null," +
                "\"8\":null,\"9\":null,\"10\":null,\"11\":null,\"12\":null,\"13\":null,\"14\":null,\"15\":null," +
                "\"16\":null}}");
            JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                  .put("status", Status.SUCCESS)
                                                  .put("resource", data);
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"));
        }));
    }

    @Test
    public void test_get_point_data(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"priority\":8,\"value\":10,\"point\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\"," +
            "\"priority_values\":{\"1\":null,\"2\":null,\"3\":null,\"4\":null,\"5\":10,\"6\":9,\"7\":null,\"8\":10," +
            "\"9\":null,\"10\":null,\"11\":null,\"12\":null,\"13\":null,\"14\":null,\"15\":null,\"16\":null}}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, true, expected, PointValueService.class.getName(), EventAction.GET_ONE, req);
    }

    @Test
    public void test_get_list_point_data_that_unsupported(TestContext context) {
        JsonObject expected = new JsonObject().put("code", ErrorCode.STATE_ERROR)
                                              .put("message", "Unsupported event GET_LIST");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, false, expected, PointValueService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_history_data_by_point(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"histories\":[{\"id\":4,\"time\":\"2019-08-10T09:22Z\",\"value\":42.0,\"priority\":16},{\"id\":3," +
            "\"time\":\"2019-08-10T09:20Z\",\"value\":32.0,\"priority\":16},{\"id\":2,\"time\":\"2019-08-10T09:18Z\"," +
            "\"value\":35.0,\"priority\":16},{\"id\":1,\"time\":\"2019-08-10T09:15Z\",\"value\":30.0," +
            "\"priority\":16}]}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, true, expected, HistoryDataService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_history_data_by_point_sort_by_acs(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"histories\":[{\"id\":1,\"time\":\"2019-08-10T09:15Z\",\"value\":30.0,\"priority\":16},{\"id\":2," +
            "\"time\":\"2019-08-10T09:18Z\",\"value\":35.0,\"priority\":16},{\"id\":3,\"time\":\"2019-08-10T09:20Z\"," +
            "\"value\":32.0,\"priority\":16},{\"id\":4,\"time\":\"2019-08-10T09:22Z\",\"value\":42.0," +
            "\"priority\":16}]}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .sort(Sort.builder().item("time", SortType.ASC).build())
                                     .build();
        asserter(context, true, expected, HistoryDataService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_history_data(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"histories\":[{\"id\":4,\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\"," +
            "\"time\":\"2019-08-10T09:22Z\",\"value\":42.0,\"priority\":16},{\"id\":3," +
            "\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\",\"time\":\"2019-08-10T09:20Z\",\"value\":32.0," +
            "\"priority\":16},{\"id\":8,\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\"," +
            "\"time\":\"2019-08-10T09:18:15Z\",\"value\":20.6,\"priority\":16},{\"id\":2," +
            "\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\",\"time\":\"2019-08-10T09:18Z\",\"value\":35.0," +
            "\"priority\":16},{\"id\":7,\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\"," +
            "\"time\":\"2019-08-10T09:17:15Z\",\"value\":20.8,\"priority\":16},{\"id\":6," +
            "\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\",\"time\":\"2019-08-10T09:16:15Z\",\"value\":20.8," +
            "\"priority\":16},{\"id\":5,\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\"," +
            "\"time\":\"2019-08-10T09:15:15Z\",\"value\":20.5,\"priority\":16},{\"id\":1," +
            "\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\",\"time\":\"2019-08-10T09:15Z\",\"value\":30.0," +
            "\"priority\":16}]}");
        RequestData req = RequestData.builder().build();
        asserter(context, true, expected, HistoryDataService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_patch_point_data(TestContext context) throws InterruptedException {
        DeliveryEvent event = createPointEvent(EventAction.CREATE,
                                               new PointValueData().setPoint(PrimaryKey.P_BACNET_SWITCH)
                                                                   .setPriority(5)
                                                                   .setValue(24d), true);
        CountDownLatch latch = new CountDownLatch(1);
        Async async = context.async(1);
        controller().fire(event, EventbusHelper.replyAsserter(context, body -> {
            latch.countDown();
            JsonObject data = new JsonObject(
                "{\"point\":\"" + PrimaryKey.P_BACNET_SWITCH + "\",\"value\":24,\"priority\":5," +
                "\"priority_values\":{\"1\":null,\"2\":null,\"3\":null,\"4\":null,\"5\":24,\"6\":null,\"7\":null," +
                "\"8\":null,\"9\":null,\"10\":null,\"11\":null,\"12\":null,\"13\":null,\"14\":null,\"15\":null," +
                "\"16\":null},\"time_audit\":{\"created_by\":\"UNDEFINED\",\"revision\":1}," +
                "\"sync_audit\":{\"status\":\"INITIAL\",\"data\":{\"message\":\"Not yet synced new resource\"}}}");
            JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                  .put("status", Status.SUCCESS)
                                                  .put("resource", data);
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"), JSONCompareMode.LENIENT);
        }));
        latch.await(TestHelper.TEST_TIMEOUT_SEC / 3, TimeUnit.SECONDS);
        DeliveryEvent event2 = createPointEvent(EventAction.PATCH,
                                                new PointValueData().setPoint(PrimaryKey.P_BACNET_SWITCH)
                                                                    .setPriority(9)
                                                                    .setValue(null), true);
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
