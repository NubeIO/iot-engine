package com.nubeiot.edge.module.datapoint.service;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import io.github.zero.utils.UUID64;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

import lombok.NonNull;

public class PointByThingServiceReaderTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_list_point_by_thing(TestContext context) {
        final Point search = MockData.search(PrimaryKey.P_BACNET_FAN);
        final JsonObject expected = constructListPoints(Collections.singletonMap(4, search));
        final JsonObject reqBody = new JsonObject().put("thing_id", UUID64.uuidToBase64(PrimaryKey.THING_FAN_HVAC));

        asserter(context, true, expected, PointByThingService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().body(reqBody).build());
    }

    @Test
    public void test_get_list_point_by_thing_n_device(TestContext context) {
        final Point search = MockData.search(PrimaryKey.P_BACNET_FAN);
        final JsonObject expected = constructListPoints(Collections.singletonMap(4, search));
        final JsonObject reqBody = new JsonObject().put("thing_id", UUID64.uuidToBase64(PrimaryKey.THING_FAN_HVAC))
                                                   .put("device_id", PrimaryKey.DEVICE_HVAC.toString());

        asserter(context, true, expected, PointByThingService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().body(reqBody).build());
    }

    @Test
    public void test_get_list_point_by_thing_n_device_n_network(TestContext context) {
        final Point search = MockData.search(PrimaryKey.P_BACNET_FAN);
        final JsonObject expected = constructListPoints(Collections.singletonMap(4, search));
        final JsonObject reqBody = new JsonObject().put("thing_id", UUID64.uuidToBase64(PrimaryKey.THING_FAN_HVAC))
                                                   .put("device_id", PrimaryKey.DEVICE_HVAC.toString())
                                                   .put("network_id", PrimaryKey.BACNET_NETWORK.toString());

        asserter(context, true, expected, PointByThingService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().body(reqBody).build());
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
