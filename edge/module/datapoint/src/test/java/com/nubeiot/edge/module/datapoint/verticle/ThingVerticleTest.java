package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class ThingVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Device_Equip_Thing();
    }

    @Test
    public void test_get_thing_success(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"code\":\"HVAC-XYZ-FAN\",\"device_id\":\"" + PrimaryKey.DEVICE_HVAC + "\",\"id\":\"" +
            PrimaryKey.THING_FAN_HVAC + "\",\"label\":{\"label\":\"HVAC Fan\"},\"type\":\"SENSOR\"," +
            "\"category\":\"VELOCITY\",\"measure_unit\":\"revolutions_per_minute\"}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/thing/" + PrimaryKey.THING_FAN_HVAC, 200, expected);
    }

    @Test
    public void test_get_thing_by_device_success(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"code\":\"HVAC-XYZ-FAN-CONTROL\",\"id\":\"" + PrimaryKey.THING_SWITCH_HVAC + "\",\"label\":{\"label\":" +
            "\"HVAC Fan Control\"},\"type\":\"ACTUATOR\",\"category\":\"SWITCH\",\"measure_unit\":\"bool\"}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/device/" + PrimaryKey.DEVICE_HVAC + "/thing/" + PrimaryKey.THING_SWITCH_HVAC, 200,
                           expected);
    }

}
