package com.nubeiot.edge.connector.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.connector.datapoint.MockData;

public class EquipServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Equip_Thing();
    }

    @Test
    public void test_get_list_equip(TestContext context) {
        JsonObject expected = new JsonObject("{\"equipments\":[{\"id\":\"e43aa03a-4746-4fb5-815d-ee62f709b535\"," +
                                             "\"code\":\"DROPLET_01\",\"type\":\"DROPLET\"," +
                                             "\"manufacturer\":\"NubeIO\"}," +
                                             "{\"id\":\"28a4ba1b-154d-4bbf-8537-320be70e50e5\",\"code\":\"HVAC_XYZ\"," +
                                             "\"type\":\"HVAC\",\"manufacturer\":\"Lennox\"}]}");
        asserter(context, true, expected, EquipmentService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build());
    }

}
