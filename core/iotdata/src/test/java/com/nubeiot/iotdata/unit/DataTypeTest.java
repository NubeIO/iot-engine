package com.nubeiot.iotdata.unit;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;

public class DataTypeTest {

    @Test
    public void test_serialize_dataType() throws JSONException {
        JSONAssert.assertEquals("{\"type\":\"number\"}", DataType.NUMBER.toJson().encode(), JSONCompareMode.STRICT);
        JSONAssert.assertEquals("{\"type\":\"percentage\", \"symbol\": \"%\"}", DataType.PERCENTAGE.toJson().encode(),
                                JSONCompareMode.STRICT);
        JSONAssert.assertEquals("{\"type\":\"celsius\", \"symbol\": \"U+2103\"}", DataType.CELSIUS.toJson().encode(),
                                JSONCompareMode.STRICT);
        JSONAssert.assertEquals("{\"type\":\"voltage\", \"symbol\": \"V\"}", DataType.VOLTAGE.toJson().encode(),
                                JSONCompareMode.STRICT);
        JSONAssert.assertEquals("{\"type\":\"dBm\", \"symbol\": \"dBm\"}", DataType.DBM.toJson().encode(),
                                JSONCompareMode.STRICT);
        JSONAssert.assertEquals("{\"type\":\"hPa\", \"symbol\": \"hPa\"}", DataType.HPA.toJson().encode(),
                                JSONCompareMode.STRICT);
        JSONAssert.assertEquals("{\"type\":\"lux\", \"symbol\": \"lx\"}", DataType.LUX.toJson().encode(),
                                JSONCompareMode.STRICT);
        JSONAssert.assertEquals("{\"type\":\"kWh\", \"symbol\": \"kWh\"}", DataType.KWH.toJson().encode(),
                                JSONCompareMode.STRICT);
        JSONAssert.assertEquals("{\"type\":\"bool\", \"possible_values\":{\"0.5\":[\"true\",\"on\",\"start\",\"1\"]," +
                                "\"0.0\":[\"false\",\"off\",\"stop\",\"0\",\"null\"]}}",
                                DataType.BOOLEAN.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_deserialize_numberType() {
        assertNumberDataType("{\"type\":\"number\"}", "number", null);
        assertNumberDataType("{\"type\":\"percentage\"}", "percentage", "%");
        assertNumberDataType("{\"type\":\"celsius\"}", "celsius", "U+2103");
        assertNumberDataType("{\"type\":\"voltage\"}", "voltage", "V");
        assertNumberDataType("{\"type\":\"dBm\"}", "dBm", "dBm");
        assertNumberDataType("{\"type\":\"hPa\"}", "hPa", "hPa");
        assertNumberDataType("{\"type\":\"lux\"}", "lux", "lx");
        assertNumberDataType("{\"type\":\"kWh\"}", "kWh", "kWh");
        assertNumberDataType("{\"type\":\"unknown\", \"symbol\": \"xxx\"}", "unknown", "xxx");
    }

    @Test
    public void test_deserialize_booleanType() throws JSONException {
        assertBooleanDataType("{\"type\":\"bool\"}", new JsonObject(
            "{\"0.5\":[\"true\",\"on\",\"start\",\"1\"]," + "\"0.0\":[\"false\",\"off\",\"stop\",\"0\",\"null\"]}"));
        assertBooleanDataType("{\"type\":\"bool\", \"possible_values\":{\"0.5\":[\"true\",\"on\",\"1\"]," +
                              "\"0.0\":[\"false\",\"off\",\"0\",\"null\"]}}", new JsonObject(
            "{\"0.5\":[\"true\",\"on\",\"1\"]," + "\"0.0\":[\"false\",\"off\",\"0\",\"null\"]}"));
    }

    private void assertNumberDataType(String from, String type, String unit) {
        final DataType dt = JsonData.from(from, DataType.class);
        Assert.assertTrue(dt instanceof NumberDataType);
        Assert.assertEquals(type, dt.type());
        Assert.assertEquals(unit, dt.unit());
        Assert.assertTrue(dt.possibleValues().isEmpty());
    }

    private void assertBooleanDataType(String from, JsonObject possibleValues) throws JSONException {
        final DataType dt = JsonData.from(from, DataType.class);
        Assert.assertTrue(dt instanceof NumberDataType);
        Assert.assertEquals("bool", dt.type());
        Assert.assertNull(dt.unit());
        JSONAssert.assertEquals(possibleValues.encode(), JsonObject.mapFrom(dt.possibleValues()).encode(),
                                JSONCompareMode.STRICT);
    }

}
