package com.nubeiot.edge.module.datapoint.verticle;

import org.junit.Test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

import com.nubeiot.edge.module.datapoint.BaseDataPointVerticleTest;
import com.nubeiot.edge.module.datapoint.MockData;
import com.nubeiot.edge.module.datapoint.MockData.PrimaryKey;

public class TransducerByPointVerticleTest extends BaseDataPointVerticleTest {

    @Override
    protected JsonObject builtinData() {
        return MockData.data_Point_Setting_Tag();
    }

    @Test
    public void test_get_transducer_by_point_200(TestContext context) {
        final JsonObject expected = new JsonObject(
            "{\"id\":2,\"transducer\":{\"id\":\"" + PrimaryKey.TRANSDUCER_TEMP_DROPLET +
            "\",\"code\":\"DROPLET-2CB2B763-T\",\"type\":\"SENSOR\",\"category\":\"TEMP\"," +
            "\"label\":{\"label\":\"Droplet Temp\"},\"measure_unit\":\"celsius\"}}");
        assertRestByClient(context, HttpMethod.GET,
                           "/api/s/network/default/point/" + PrimaryKey.P_GPIO_TEMP + "/transducer/" +
                           PrimaryKey.TRANSDUCER_TEMP_DROPLET, 200, expected);
    }

}
