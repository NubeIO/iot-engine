package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.github.zero88.jpa.Sortable.Direction;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.Sort;
import com.nubeiot.core.event.DeliveryEvent;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.dto.HistorySettingType;
import com.nubeiot.iotdata.edge.model.tables.pojos.HistorySetting;
import com.nubeiot.iotdata.edge.model.tables.pojos.PointValueData;

public class HistoryDataServiceTest extends AbstractPointDataServiceTest {

    @Override
    protected String settingAddress() {
        return HistorySettingService.class.getName();
    }

    @Override
    protected String dataAddress() {
        return HistoryDataService.class.getName();
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
    public void test_assert_new_history_data(TestContext context) {
        final Async async = context.async();
        final UUID point = PrimaryKey.P_BACNET_FAN;
        final PointValueData pv = new PointValueData().setPoint(point).setPriority(12).setValue(500d);
        final DeliveryEvent pvEvent = PointValueServiceTest.createPointEvent(pv, false);
        final JsonObject setting = createHisSetting(point);
        enabledSettingThenAddPV(context, setting, pvEvent).flatMap(ignore -> doQuery(point)).doOnSuccess(msg -> {
            JsonHelper.assertJson(context, async,
                                  new JsonObject("{\"histories\":[{\"id\":9,\"value\":500.0,\"priority\":12}]}"),
                                  msg.getData(), JsonHelper.ignore("histories.[].time"));
        }).subscribe();
    }

    @Test
    public void test_patch_point_data_not_exceed_cov(TestContext context) {
        final UUID point = PrimaryKey.P_GPIO_TEMP;
        final PointValueData secondPV = new PointValueData().setPoint(point).setPriority(8).setValue(27.5d);
        final JsonObject expected = new JsonObject("{\"histories\":[{\"id\":9,\"value\":28.0,\"priority\":5}]}");
        patchPointValue_Then_AssertHistory(context, point, secondPV, expected);
    }

    @Test
    public void test_patch_point_data_exceed_cov(TestContext context) {
        final UUID point = PrimaryKey.P_GPIO_TEMP;
        final PointValueData secondPV = new PointValueData().setPoint(point).setPriority(8).setValue(25.7);
        final JsonObject expected = new JsonObject(
            "{\"histories\":[{\"id\":9,\"value\":28.0,\"priority\":5},{\"id\":10,\"value\":25.7,\"priority\":8}]}");
        patchPointValue_Then_AssertHistory(context, point, secondPV, expected);
    }

    @Test
    public void test_patch_point_data_null_not_exceed_cov(TestContext context) {
        final UUID point = PrimaryKey.P_GPIO_TEMP;
        final PointValueData secondPV = new PointValueData().setPoint(point).setPriority(8).setValue(null);
        final JsonObject expected = new JsonObject("{\"histories\":[{\"id\":9,\"value\":28.0,\"priority\":5}]}");
        patchPointValue_Then_AssertHistory(context, point, secondPV, expected);
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
                                     .sort(Sort.builder().item("time", Direction.ASC).build())
                                     .build();
        asserter(context, true, expected, HistoryDataService.class.getName(), EventAction.GET_LIST, req);
    }

    private JsonObject createHisSetting(UUID point) {
        final HistorySetting historySetting = new HistorySetting().setEnabled(true)
                                                                  .setType(HistorySettingType.COV)
                                                                  .setTolerance(1.0);
        return JsonPojo.from(historySetting).toJson().put("point_id", point.toString());
    }

    private void patchPointValue_Then_AssertHistory(TestContext context, UUID point, PointValueData secondPV,
                                                    JsonObject expected) {
        final Async async = context.async();
        final PointValueData firstPV = new PointValueData().setPoint(point).setPriority(5).setValue(28d);
        final DeliveryEvent first = PointValueServiceTest.createPointEvent(firstPV, false);
        final DeliveryEvent secondEvent = PointValueServiceTest.createPointEvent(secondPV, false);
        final JsonObject s = createHisSetting(point);
        enabledSettingThenAddPV(context, s, first).flatMap(ignore -> addPV(secondEvent))
                                                  .delay(1, TimeUnit.SECONDS)
                                                  .flatMap(ignore -> doQuery(point))
                                                  .doOnSuccess(msg -> {
                                                      JsonHelper.assertJson(context, async, expected, msg.getData(),
                                                                            JsonHelper.ignore("histories.[].time"));
                                                  })
                                                  .subscribe();
    }

}
