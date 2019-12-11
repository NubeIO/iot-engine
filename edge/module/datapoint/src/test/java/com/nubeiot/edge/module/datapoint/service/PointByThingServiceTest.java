package com.nubeiot.edge.module.datapoint.service;

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

public class PointByThingServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_list_point_by_thing(TestContext context) {
        final Point search = MockData.search(PrimaryKey.P_BACNET_FAN);
        final JsonObject expected = new JsonObject().put("points", new JsonArray().add(
            new JsonObject().put("id", 4).put("point", JsonPojo.from(search).toJson())));
        final RequestData req = RequestData.builder()
                                           .body(new JsonObject().put("thing_id",
                                                                      UUID64.uuidToBase64(PrimaryKey.THING_FAN_HVAC)))
                                           .build();
        asserter(context, true, expected, PointByThingService.class.getName(), EventAction.GET_LIST, req);
    }

}
