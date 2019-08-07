package com.nubeiot.edge.connector.datapoint;

import org.json.JSONException;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;

public class DataPointConfigTest {

    @Test
    public void serialize_default_config() throws JSONException {
        final JsonObject expected = new JsonObject(
            "{\"lowdb_migration\":{\"enabled\":false},\"__publisher__\":{\"type\":\"\",\"enabled\":false}," +
            "\"builtin_data\":{\"measure_unit\":[{\"type\":\"number\"},{\"type\":\"percentage\",\"symbol\":\"%\"}," +
            "{\"type\":\"voltage\",\"symbol\":\"V\"},{\"type\":\"celsius\",\"symbol\":\"U+2103\"},{\"type\":\"bool\"," +
            "\"possible_values\":{\"0.5\":[\"true\",\"on\",\"start\",\"1\"],\"0.0\":[\"false\"," + "\"off\",\"stop\"," +
            "\"0\",\"null\"]}},{\"type\":\"dBm\",\"symbol\":\"dBm\"},{\"type\":\"hPa\",\"symbol\":\"hPa\"}," +
            "{\"type\":\"lux\",\"symbol\":\"lx\"},{\"type\":\"kWh\",\"symbol\":\"kWh\"}]}}");
        JsonHelper.assertJson(expected, DataPointConfig.def().toJson());
    }

    @Test
    public void deserialize_no_data() throws JSONException {
        final JsonObject expected = new JsonObject("{\"lowdb_migration\":{\"enabled\":false}}");
        JsonHelper.assertJson(expected, new DataPointConfig().toJson());
    }

}
