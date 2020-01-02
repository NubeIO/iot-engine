package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.PointPriorityValue;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

import lombok.NonNull;

public class PointHistoryRTDataServiceTest extends BaseDataPointServiceTest {

    private PointValueData firstValue;

    @Override
    protected void setup(TestContext context) {
        super.setup(context);
        firstValue = new PointValueData().setPriority(5).setValue(28d).setPoint(PrimaryKey.P_GPIO_TEMP);
        createDataPoint(context, EventAction.CREATE, firstValue,
                        new PointValueData(firstValue).setPriorityValues(new PointPriorityValue().add(5, 28)));
    }

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_assert_history_data(TestContext context) {
        JsonObject req = new JsonObject().put("point_id", PrimaryKey.P_GPIO_TEMP.toString());
        Async async = context.async();
        controller().fire(DeliveryEvent.builder()
                                       .address(HistoryDataService.class.getName())
                                       .action(EventAction.GET_LIST)
                                       .payload(RequestData.builder().body(req).build().toJson())
                                       .build(), EventbusHelper.replyAsserter(context, body -> {
            JsonObject expected = new JsonObject("{\"histories\":[{\"id\":9,\"value\":28.0,\"priority\":5}]}");
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"),
                                  JsonHelper.ignore("histories.[].time"));
        }));
    }

    @Test
    public void test_patch_point_data_not_exceed_cov(TestContext context) {
        final PointValueData pv = new PointValueData().setPriority(8).setValue(27.5d).setPoint(PrimaryKey.P_GPIO_TEMP);
        createDataPoint(context, EventAction.PATCH, pv, new PointValueData(firstValue).setPriorityValues(
            new PointPriorityValue().add(firstValue.getPriority(), firstValue.getValue()).add(8, 27.5)));
        JsonObject req = new JsonObject().put("point_id", PrimaryKey.P_GPIO_TEMP.toString());
        Async async = context.async();
        controller().fire(DeliveryEvent.builder()
                                       .address(HistoryDataService.class.getName())
                                       .action(EventAction.GET_LIST)
                                       .payload(RequestData.builder().body(req).build().toJson())
                                       .build(), EventbusHelper.replyAsserter(context, body -> {
            JsonObject expected = new JsonObject("{\"histories\":[{\"id\":9,\"value\":28.0,\"priority\":5}]}");
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"),
                                  JsonHelper.ignore("histories.[].time"));
        }));
    }

    @Test
    public void test_patch_point_data_exceed_cov(TestContext context) {
        final PointValueData pv = new PointValueData().setPriority(8).setValue(25.7).setPoint(PrimaryKey.P_GPIO_TEMP);
        createDataPoint(context, EventAction.PATCH, pv, new PointValueData(firstValue).setPriorityValues(
            new PointPriorityValue().add(firstValue.getPriority(), firstValue.getValue()).add(8, 25.7)));
        JsonObject req = new JsonObject().put("point_id", PrimaryKey.P_GPIO_TEMP.toString());
        Async async = context.async();
        controller().fire(DeliveryEvent.builder()
                                       .address(HistoryDataService.class.getName())
                                       .action(EventAction.GET_LIST)
                                       .payload(RequestData.builder().body(req).build().toJson())
                                       .build(), EventbusHelper.replyAsserter(context, body -> {
            JsonObject expected = new JsonObject("{\"histories\":[{\"id\":9,\"value\":28.0,\"priority\":5}," +
                                                 "{\"id\":10,\"value\":25.7,\"priority\":8}]}");
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"),
                                  JsonHelper.ignore("histories.[].time"));
        }));
    }

    @Test
    public void test_assert_realtime_data(TestContext context) {
        JsonObject req = new JsonObject().put("point_id", PrimaryKey.P_GPIO_TEMP.toString());
        Async async = context.async();
        controller().fire(DeliveryEvent.builder()
                                       .address(RealtimeDataService.class.getName())
                                       .action(EventAction.GET_LIST)
                                       .payload(RequestData.builder().body(req).build().toJson())
                                       .build(), EventbusHelper.replyAsserter(context, body -> {
            JsonObject expected = new JsonObject(
                "{\"rt_data\":[{\"id\":1,\"value\":{\"val\":28.0,\"priority\":5,\"display\":\"28.0 °C\"}}]}");
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"),
                                  JsonHelper.ignore("rt_data.[].time"));
        }));
    }

    @Test
    public void test_assert_realtime_data_without_enable(TestContext context) {
        PointValueData another = new PointValueData(firstValue).setPoint(PrimaryKey.P_BACNET_SWITCH);
        createDataPoint(context, EventAction.CREATE, another,
                        new PointValueData(another).setPriorityValues(new PointPriorityValue().add(5, 28)));
        JsonObject req = new JsonObject().put("point_id", PrimaryKey.P_BACNET_SWITCH.toString());
        Async async = context.async();
        controller().fire(DeliveryEvent.builder()
                                       .address(RealtimeDataService.class.getName())
                                       .action(EventAction.GET_LIST)
                                       .payload(RequestData.builder().body(req).build().toJson())
                                       .build(), EventbusHelper.replyAsserter(context, body -> {
            JsonHelper.assertJson(context, async, new JsonObject("{\"rt_data\":[]}"), body.getJsonObject("data"));
        }));
    }

    private void createDataPoint(@NonNull TestContext context, @NonNull EventAction action, @NonNull PointValueData pv,
                                 @NonNull PointValueData output) {
        final DeliveryEvent event = PointDataServiceTest.createPointEvent(action, pv, false);
        final Async async = context.async();
        controller().fire(event, EventbusHelper.replyAsserter(context, body -> {
            JsonObject result = JsonPojo.from(output).toJson();
            JsonObject expected = new JsonObject().put("action", event.getAction())
                                                  .put("status", Status.SUCCESS).put("resource", result);
            JsonHelper.assertJson(context, async, expected, body.getJsonObject("data"));
        }));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            context.fail(e);
        }
    }

}
