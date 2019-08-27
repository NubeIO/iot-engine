package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.skyscreamer.jsonassert.Customization;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper;
import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.pojos.JsonPojo;
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

    @Test
    public void test_create_point_data(TestContext context) throws InterruptedException {
        JsonObject req = new JsonObject().put("point_id", PrimaryKey.P_GPIO_TEMP.toString());
        DeliveryEvent event = createEvent(req, new PointValueData().setPriority(5).setValue(24d), EventAction.CREATE);
        CountDownLatch latch = new CountDownLatch(1);
        Async async = context.async(2);
        controller().request(event, EventbusHelper.replyAsserter(context, body -> {
            latch.countDown();
            JsonObject data = new JsonObject(
                "{\"point\":\"" + PrimaryKey.P_GPIO_TEMP + "\",\"value\":24.0,\"priority\":5,\"priority_values\":" +
                "{\"5\":24.0},\"time_audit\":{\"created_by\":\"UNKNOWN\"},\"sync_audit\":{\"synced\":false}}");
            JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                  .put("status", Status.SUCCESS)
                                                  .put("resource", data);
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"),
                                  new Customization("time_audit.created_time", (o1, o2) -> false));
        }));
        latch.await(TestHelper.TEST_TIMEOUT_SEC / 2, TimeUnit.SECONDS);
        //FOR UPDATE HISTORY DATA
        Thread.sleep(500);
        controller().request(DeliveryEvent.builder()
                                          .address(HistoryDataService.class.getName())
                                          .action(EventAction.GET_LIST)
                                          .payload(RequestData.builder().body(req).build().toJson())
                                          .build(), EventbusHelper.replyAsserter(context, body -> {
            JsonObject expected = new JsonObject("{\"histories\":[{\"id\":9,\"value\":24.0,\"priority\":5}]}");
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"),
                                  new Customization("histories.[].time", (o1, o2) -> false));
        }));
    }

    @Test
    public void test_patch_point_data(TestContext context) throws InterruptedException {
        JsonObject req = new JsonObject().put("point_id", PrimaryKey.P_BACNET_SWITCH.toString());
        DeliveryEvent event = createEvent(req, new PointValueData().setPriority(5).setValue(24d), EventAction.CREATE);
        CountDownLatch latch = new CountDownLatch(2);
        Async async = context.async(3);
        controller().request(event, EventbusHelper.replyAsserter(context, body -> {
            latch.countDown();
            JsonObject data = new JsonObject(
                "{\"point\":\"" + PrimaryKey.P_BACNET_SWITCH + "\",\"value\":24.0,\"priority\":5,\"priority_values\":" +
                "{\"5\":24.0},\"time_audit\":{\"created_by\":\"UNKNOWN\"},\"sync_audit\":{\"synced\":false}}");
            JsonObject expected = new JsonObject().put("action", EventAction.CREATE)
                                                  .put("status", Status.SUCCESS)
                                                  .put("resource", data);
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"),
                                  new Customization("time_audit.created_time", (o1, o2) -> false));
        }));
        latch.await(TestHelper.TEST_TIMEOUT_SEC / 3, TimeUnit.SECONDS);
        //FOR UPDATE HISTORY DATA
        Thread.sleep(500);
        DeliveryEvent event2 = createEvent(req, new PointValueData().setPriority(9).setValue(28d), EventAction.PATCH);
        controller().request(event2, EventbusHelper.replyAsserter(context, body -> {
            latch.countDown();
            JsonObject data = new JsonObject(
                "{\"point\":\"" + PrimaryKey.P_BACNET_SWITCH + "\",\"value\":28.0,\"priority\":9," +
                "\"priority_values\":{\"5\":24.0,\"9\":28.0}," + "\"time_audit\":{\"created_by\":\"UNKNOWN\"," +
                "\"last_modified_by\":\"UNKNOWN\"}}");
            JsonObject expected = new JsonObject().put("action", EventAction.PATCH)
                                                  .put("status", Status.SUCCESS)
                                                  .put("resource", data);
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"),
                                  new Customization("time_audit.created_time", (o1, o2) -> false),
                                  new Customization("time_audit.last_modified_time", (o1, o2) -> false));
        }));
        latch.await(TestHelper.TEST_TIMEOUT_SEC / 3, TimeUnit.SECONDS);
        //FOR UPDATE HISTORY DATA
        Thread.sleep(500);
        controller().request(DeliveryEvent.builder()
                                          .address(HistoryDataService.class.getName())
                                          .action(EventAction.GET_LIST)
                                          .payload(RequestData.builder().body(req).build().toJson())
                                          .build(), EventbusHelper.replyAsserter(context, body -> {
            JsonObject expected = new JsonObject(
                "{\"histories\":[{\"id\":9,\"value\":24.0,\"priority\":5},{\"id\":10,\"value\":28.0,\"priority\":9}]}");
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"),
                                  new Customization("histories.[].time", (o1, o2) -> false));
        }));
    }

    @Test
    public void test_get_point_data(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"point\":\"" + PrimaryKey.P_GPIO_HUMIDITY + "\",\"priority\":8,\"value\":10.0," +
            "\"priority_values\":{\"5\":10.0,\"6\":9.0,\"8\":10.0}}");
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
            "{\"histories\":[{\"id\":1,\"time\":\"2019-08-10T09:15Z\",\"value\":30.0},{\"id\":2," +
            "\"time\":\"2019-08-10T09:18Z\",\"value\":35.0},{\"id\":3,\"time\":\"2019-08-10T09:20Z\",\"value\":32.0}," +
            "{\"id\":4,\"time\":\"2019-08-10T09:22Z\",\"value\":42.0}]}");
        RequestData req = RequestData.builder()
                                     .body(new JsonObject().put("point_id", PrimaryKey.P_GPIO_HUMIDITY.toString()))
                                     .build();
        asserter(context, true, expected, HistoryDataService.class.getName(), EventAction.GET_LIST, req);
    }

    @Test
    public void test_get_history_data(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"histories\":[{\"id\":1,\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\"," +
            "\"time\":\"2019-08-10T09:15Z\",\"value\":30.0},{\"id\":2," +
            "\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\",\"time\":\"2019-08-10T09:18Z\",\"value\":35.0}," +
            "{\"id\":3,\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\",\"time\":\"2019-08-10T09:20Z\",\"value\":32" +
            ".0},{\"id\":4,\"point\":\"3bea3c91-850d-4409-b594-8ffb5aa6b8a0\",\"time\":\"2019-08-10T09:22Z\"," +
            "\"value\":42.0},{\"id\":5,\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\"," +
            "\"time\":\"2019-08-10T09:15:15Z\",\"value\":20.5},{\"id\":6," +
            "\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\",\"time\":\"2019-08-10T09:16:15Z\",\"value\":20.8}," +
            "{\"id\":7,\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\",\"time\":\"2019-08-10T09:17:15Z\"," +
            "\"value\":20.8},{\"id\":8,\"point\":\"edbe3acf-5fca-4672-b633-72aa73004917\"," +
            "\"time\":\"2019-08-10T09:18:15Z\",\"value\":20.6}]}");
        RequestData req = RequestData.builder().build();
        asserter(context, true, expected, HistoryDataService.class.getName(), EventAction.GET_LIST, req);
    }

    private DeliveryEvent createEvent(JsonObject req, PointValueData pv1, EventAction action) {
        JsonObject create = JsonPojo.from(pv1).toJson().mergeIn(req, true);
        return DeliveryEvent.builder()
                            .address(PointValueService.class.getName())
                            .action(action)
                            .payload(RequestData.builder().body(create).build().toJson())
                            .build();
    }

}
