package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class TransducerVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Device_Transducer();
    }

    @Test
    public void test_get_transducer_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"code\":\"HVAC-XYZ-FAN\",\"device_id\":\"" + PrimaryKey.DEVICE_HVAC + "\",\"id\":\"" +
            PrimaryKey.TRANSDUCER_FAN_HVAC + "\",\"label\":{\"label\":\"HVAC Fan\"},\"type\":\"SENSOR\"," +
            "\"category\":\"VELOCITY\",\"measure_unit\":\"revolutions_per_minute\"}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/transducer/" + PrimaryKey.TRANSDUCER_FAN_HVAC, 200,
                           expected);
    }

    @Test
    public void test_get_transducer_by_device_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"code\":\"HVAC-XYZ-FAN-CONTROL\",\"id\":\"" + PrimaryKey.TRANSDUCER_SWITCH_HVAC +
            "\",\"label\":{\"label\":\"HVAC Fan Control\"},\"type\":\"ACTUATOR\",\"category\":\"SWITCH\"," +
            "\"measure_unit\":\"bool\"}");
        assertRestByClient(context, HttpMethod.GET, "/api/s/device/" + PrimaryKey.DEVICE_HVAC + "/transducer/" +
                                                    PrimaryKey.TRANSDUCER_SWITCH_HVAC, 200, expected);
    }

    @Test
    public void test_get_transducer_by_device_n_network_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"code\":\"HVAC-XYZ-FAN-CONTROL\",\"id\":\"" + PrimaryKey.TRANSDUCER_SWITCH_HVAC +
            "\",\"label\":{\"label\":\"HVAC Fan Control\"},\"type\":\"ACTUATOR\",\"category\":\"SWITCH\"," +
            "\"measure_unit\":\"bool\"}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/network/" + PrimaryKey.BACNET_NETWORK + "/device/" + PrimaryKey.DEVICE_HVAC +
                           "/transducer/" + PrimaryKey.TRANSDUCER_SWITCH_HVAC, 200, expected);
    }

    @Test
    public void test_get_transducer_by_network_only_should_404(TestContext context) {
        final JsonObject expected = new JsonObject().put("message", "Resource not found").put("uri", "");
        assertRestByClient(context, HttpMethod.GET, "/api/s/network/" + PrimaryKey.BACNET_NETWORK + "/transducer/" +
                                                    PrimaryKey.TRANSDUCER_SWITCH_HVAC, 404, expected,
                           JsonHelper.ignore("uri"));
    }

}
