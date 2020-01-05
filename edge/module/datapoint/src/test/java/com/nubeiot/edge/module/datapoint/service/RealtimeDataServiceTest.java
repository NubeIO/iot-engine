package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.EventbusHelper;
import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.PointPriorityValue;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

public class RealtimeDataServiceTest extends BaseDataPointServiceTest {

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
        PointValueServiceTest.createPointValue(controller(), context, EventAction.CREATE, another,
                                               new PointValueData(another).setPriorityValues(
                                                   new PointPriorityValue().add(5, 28)));
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

}
