package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.github.zero.utils.UUID64;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class ThingByPointServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_things_by_point(TestContext context) {
        final JsonObject thing = new JsonObject(
            "{\"things\":[{\"id\":1,\"thing\":{\"id\":\"" + PrimaryKey.THING_HUMIDITY_DROPLET + "\",\"code\":" +
            "\"DROPLET-2CB2B763-H\",\"type\":\"SENSOR\",\"category\":\"HUMIDITY\",\"label\":{\"label\":\"Droplet " +
            "Humidity\"},\"measure_unit\":\"percentage\"}}]}");
        final JsonObject reqBody = new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_GPIO_HUMIDITY));
        asserter(context, true, thing, ThingByPointService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().body(reqBody).build());
    }

    @Test
    public void test_get_thing_not_assign_to_point(TestContext context) {
        final JsonObject thing = new JsonObject().put("code", ErrorCode.NOT_FOUND)
                                                 .put("message",
                                                      "Not found resource with point_id=" + PrimaryKey.P_GPIO_HUMIDITY +
                                                      " and thing_id=" + PrimaryKey.THING_TEMP_HVAC);
        final JsonObject reqBody = new JsonObject().put("point_id", UUID64.uuidToBase64(PrimaryKey.P_GPIO_HUMIDITY))
                                                   .put("thing_id", UUID64.uuidToBase64(PrimaryKey.THING_TEMP_HVAC));
        asserter(context, false, thing, ThingByPointService.class.getName(), EventAction.GET_ONE,
                 RequestData.builder().body(reqBody).build());
    }

}
