package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.Sort;
import com.nubeiot.core.dto.Sort.SortType;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.PointPriorityValue;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

public class HistoryDataServiceTest extends BaseDataPointServiceTest {

    private PointValueData firstValue;

    @Override
    protected void setup(TestContext context) {
        super.setup(context);
        firstValue = new PointValueData().setPriority(5).setValue(28d).setPoint(PrimaryKey.P_GPIO_TEMP);
        PointValueServiceTest.createPointValue(controller(), context, EventAction.CREATE, firstValue,
                                               new PointValueData(firstValue).setPriorityValues(
                                                   new PointPriorityValue().add(5, 28)));
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
        PointValueServiceTest.createPointValue(controller(), context, EventAction.PATCH, pv,
                                               new PointValueData(firstValue).setPriorityValues(
                                                   new PointPriorityValue().add(firstValue.getPriority(),
                                                                                firstValue.getValue()).add(8, 27.5)));
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
        PointValueServiceTest.createPointValue(controller(), context, EventAction.PATCH, pv,
                                               new PointValueData(firstValue).setPriorityValues(
                                                   new PointPriorityValue().add(firstValue.getPriority(),
                                                                                firstValue.getValue()).add(8, 25.7)));
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

}
