package com.nubeiot.edge.module.datapoint.service;

import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.core.utils.UUID64;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

import lombok.NonNull;

public class PointByThingServiceWriterTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_create_point_by_thing(TestContext context) {
        final UUID uuid = UUID.randomUUID();
        final Point newOne = MockData.search(PrimaryKey.P_BACNET_TEMP).setId(uuid).setCode("NEW_TEMP");
        final JsonObject expected = new JsonObject();
        final JsonObject reqBody = new JsonObject().put("thing_id", UUID64.uuidToBase64(PrimaryKey.THING_FAN_HVAC))
                                                   .put("point", JsonPojo.from(newOne).toJson());
        asserter(context, true, expected, PointByThingService.class.getName(), EventAction.CREATE,
                 RequestData.builder().body(reqBody).build());
    }

    @Test
    public void test_create_point_by_thing_n_device(TestContext context) {
    }

    @Test
    public void test_create_point_by_thing_n_device_n_network(TestContext context) {
    }

    private JsonObject constructListPoints(@NonNull Map<Integer, Point> pointThingMap) {
        final JsonArray array = pointThingMap.entrySet()
                                             .stream()
                                             .map(entry -> new JsonObject().put("id", entry.getKey())
                                                                           .put("point", JsonPojo.from(entry.getValue())
                                                                                                 .toJson()))
                                             .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
        return new JsonObject().put("points", array);
    }

}
