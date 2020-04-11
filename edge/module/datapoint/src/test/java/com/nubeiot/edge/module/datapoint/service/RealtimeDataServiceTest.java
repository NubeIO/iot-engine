package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.github.zero.utils.UUID64;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

public class RealtimeDataServiceTest extends AbstractPointDataServiceTest {

    @Override
    protected String settingAddress() { return RealtimeSettingService.class.getName(); }

    @Override
    protected String dataAddress() {
        return RealtimeDataService.class.getName();
    }

    @Test
    public void test_assert_realtime_data(TestContext context) {
        final Async async = context.async();
        final UUID point = PrimaryKey.P_GPIO_TEMP;
        final DeliveryEvent pvEvent = PointValueServiceTest.createPointEvent(
            new PointValueData().setPoint(point).setPriority(4).setValue(15d), false);
        final JsonObject setting = new JsonObject().put("enabled", true).put("point_id", UUID64.uuidToBase64(point));
        enabledSettingThenAddPV(context, setting, pvEvent).flatMap(ignore -> doQuery(point)).doOnSuccess(msg -> {
            JsonObject expected = new JsonObject(
                "{\"rt_data\":[{\"id\":1,\"value\":{\"val\":15.0,\"priority\":4,\"display\":\"15.0 °C\"}}]}");
            JsonHelper.assertJson(context, async, expected, msg.getData(), JsonHelper.ignore("rt_data.[].time"));
        }).subscribe();
    }

    @Test
    public void test_assert_realtime_data_without_enable(TestContext context) {
        final Async async = context.async();
        final PointValueData pv = new PointValueData().setPoint(PrimaryKey.P_BACNET_SWITCH)
                                                      .setPriority(12)
                                                      .setValue(22d);
        final DeliveryEvent pvEvent = PointValueServiceTest.createPointEvent(pv, false);
        addPV(pvEvent).delay(1, TimeUnit.SECONDS)
                      .flatMap(ignore -> doQuery(PrimaryKey.P_BACNET_SWITCH))
                      .doOnSuccess(msg -> JsonHelper.assertJson(context, async, new JsonObject("{\"rt_data\":[]}"),
                                                                msg.getData()))
                      .subscribe();
    }

}
