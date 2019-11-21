package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;

public class DeviceVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Device_Equip_Thing();
    }

    @Test
    public void test_get_devices(TestContext context) {
        final JsonObject expected = new JsonObject("{\"devices\":[{\"id\":1,\"device\":{\"id\":\"e43aa03a-4746-4fb5" +
                                                   "-815d-ee62f709b535\",\"code\":\"DROPLET_01\"," +
                                                   "\"type\":\"DROPLET\",\"manufacturer\":\"NubeIO\"}}]}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/network/gpio/device", 200, expected);
    }

}
