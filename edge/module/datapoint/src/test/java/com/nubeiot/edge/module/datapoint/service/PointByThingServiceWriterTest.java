package com.nubeiot.edge.module.datapoint.service;

import java.util.UUID;

import org.junit.Test;

import io.github.zero.utils.UUID64;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;
import com.nubeiot.iotdata.edge.model.tables.pojos.Point;

public class PointByThingServiceWriterTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_create_point_by_thing_actuator(TestContext context) {
        final UUID uuid = UUID.randomUUID();
        final Point newOne = MockData.search(PrimaryKey.P_BACNET_TEMP).setId(uuid).setCode("NEW_TEMP");
        final JsonObject expected = new JsonObject(
            "{\"resource\":{\"id\":6,\"point\":{\"id\":\"" + uuid + "\",\"code\":\"NEW_TEMP\",\"edge\":\"" +
            PrimaryKey.EDGE + "\",\"network\":\"" + PrimaryKey.BACNET_NETWORK + "\",\"label\":null,\"enabled\":true," +
            "\"protocol\":\"BACNET\",\"kind\":\"INPUT\",\"type\":\"DIGITAL\",\"measure_unit\":\"celsius\"," +
            "\"unit_alias\":null,\"min_scale\":null,\"max_scale\":null,\"precision\":3,\"offset\":0,\"version\":null," +
            "\"metadata\":null}},\"action\":\"CREATE\",\"status\":\"SUCCESS\"}");
        final JsonObject reqBody = new JsonObject().put("thing_id", UUID64.uuidToBase64(PrimaryKey.THING_SWITCH_HVAC))
                                                   .put("point", JsonPojo.from(newOne).toJson());
        asserter(context, true, expected, PointByThingService.class.getName(), EventAction.CREATE,
                 RequestData.builder().body(reqBody).build());
    }

    @Test
    public void test_create_point_by_duplicate_sensor(TestContext context) {
        final UUID uuid = UUID.randomUUID();
        final Point newOne = MockData.search(PrimaryKey.P_BACNET_TEMP).setId(uuid).setCode("NEW_TEMP");
        final JsonObject expected = new JsonObject().put("code", ErrorCode.INVALID_ARGUMENT)
                                                    .put("message", "Thing " + PrimaryKey.THING_TEMP_HVAC +
                                                                    " with type SENSOR is already assigned to Point " +
                                                                    PrimaryKey.P_BACNET_TEMP);
        final JsonObject reqBody = new JsonObject().put("thing_id", UUID64.uuidToBase64(PrimaryKey.THING_TEMP_HVAC))
                                                   .put("point", JsonPojo.from(newOne).toJson());
        asserter(context, false, expected, PointByThingService.class.getName(), EventAction.CREATE,
                 RequestData.builder().body(reqBody).build());
    }

}
