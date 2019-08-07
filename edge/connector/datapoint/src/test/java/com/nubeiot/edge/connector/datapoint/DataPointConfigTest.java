package com.nubeiot.edge.connector.datapoint;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;

public class DataPointConfigTest {

    @Test
    public void serialize_default() throws JSONException {
        JsonObject expected = new JsonObject(
            "{\"lowdb_migration\":{\"enabled\":false},\"builtin_data\":{\"units\":[{\"type\":\"number\"}," +
            "{\"type\":\"percentage\",\"symbol\":\"%\"},{\"type\":\"voltage\",\"symbol\":\"V\"}," +
            "{\"type\":\"celsius\",\"symbol\":\"U+2103\"},{\"type\":\"bool\",\"possible_values\":{\"0" +
            ".5\":[\"true\",\"on\",\"start\",\"1\"],\"0.0\":[\"false\",\"off\",\"stop\",\"0\",\"null\"]}}," +
            "{\"type\":\"dBm\",\"symbol\":\"dBm\"},{\"type\":\"hPa\",\"symbol\":\"hPa\"},{\"type\":\"lux\"," +
            "\"symbol\":\"lx\"},{\"type\":\"kWh\",\"symbol\":\"kWh\"}]}}");
        JSONAssert.assertEquals(expected.encode(), new DataPointConfig().toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void deserialize_default() throws JSONException {
        JSONAssert.assertEquals("{\"lowdb_migration\":{\"enabled\":false},\"builtin_data\":{}}",
                                new DataPointConfig().toJson().encode(), JSONCompareMode.STRICT);
    }

}
