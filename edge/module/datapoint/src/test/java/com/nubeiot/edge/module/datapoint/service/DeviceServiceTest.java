package com.nubeiot.edge.module.datapoint.service;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.edge.module.datapoint.BaseDataPointServiceTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class DeviceServiceTest extends BaseDataPointServiceTest {

    @Override
    protected JsonObject testData() {
        return MockData.data_Device_Equip_Thing();
    }

    @Test
    public void test_get_list_device(TestContext context) {
        JsonObject expected = new JsonObject(
            "{\"devices\":[{\"id\":\"" + PrimaryKey.DEVICE_DROPLET + "\",\"code\":\"DROPLET_01\"," +
            "\"type\":\"DROPLET\",\"protocol\":\"WIRE\",\"state\":\"NONE\",\"manufacturer\":\"NubeIO\"}," +
            "{\"id\":\"" + PrimaryKey.DEVICE_HVAC + "\",\"code\":\"HVAC_XYZ\",\"type\":\"HVAC\"," +
            "\"protocol\":\"BACNET\",\"state\":\"NONE\",\"manufacturer\":\"Lennox\"}]}");
        asserter(context, true, expected, DeviceService.class.getName(), EventAction.GET_LIST,
                 RequestData.builder().build());
    }

}
