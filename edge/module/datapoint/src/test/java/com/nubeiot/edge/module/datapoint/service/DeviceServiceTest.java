package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;

public class DeviceServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Device_Equip_Thing();
    }

    @Test
    public void test_get_list_device(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"devices\":[{\"id\":\"e43aa03a-4746-4fb5-815d-ee62f709b535\",\"code\":\"DROPLET_01\"," +
            "\"type\":\"DROPLET\",\"protocol\":\"UNKNOWN\",\"state\":\"NONE\",\"manufacturer\":\"NubeIO\"}," +
            "{\"id\":\"28a4ba1b-154d-4bbf-8537-320be70e50e5\",\"code\":\"HVAC_XYZ\",\"type\":\"HVAC\"," +
            "\"protocol\":\"UNKNOWN\",\"state\":\"NONE\",\"manufacturer\":\"Lennox\"}]}");
        asserter(context, true, expected, DeviceService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build());
    }

}
