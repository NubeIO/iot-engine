package com.nubeiot.iotdata.unit;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;

public class DataTypeTest {

    @Test
    public void test_serialize_dataType() throws JSONException {
        JsonHelper.assertJson(new JsonObject("{\"type\":\"number\", \"category\":\"ALL\"}"), DataType.NUMBER.toJson());
        JsonHelper.assertJson(new JsonObject("{\"type\":\"percentage\", \"symbol\": \"%\", \"category\":\"ALL\"}"),
                              DataType.PERCENTAGE.toJson());
        JsonHelper.assertJson(
            new JsonObject("{\"type\":\"celsius\", \"symbol\": \"U+2103\", \"category\":\"TEMPERATURE\"}"),
            DataType.CELSIUS.toJson());
        JsonHelper.assertJson(
            new JsonObject("{\"type\":\"voltage\", \"symbol\": \"V\", \"category\":\"ELECTRIC_POTENTIAL\"}"),
            DataType.VOLTAGE.toJson());
        JsonHelper.assertJson(new JsonObject("{\"type\":\"dBm\", \"symbol\": \"dBm\", \"category\":\"POWER\"}"),
                              DataType.DBM.toJson());
        JsonHelper.assertJson(new JsonObject("{\"type\":\"hPa\", \"symbol\": \"hPa\", \"category\":\"PRESSURE\"}"),
                              DataType.HPA.toJson());
        JsonHelper.assertJson(new JsonObject("{\"type\":\"lux\", \"symbol\": \"lx\", \"category\":\"ILLUMINATION\"}"),
                              DataType.LUX.toJson());
        JsonHelper.assertJson(new JsonObject("{\"type\":\"kWh\", \"symbol\": \"kWh\", \"category\":\"POWER\"}"),
                              DataType.KWH.toJson());
        JsonHelper.assertJson(new JsonObject("{\"type\":\"rpm\", \"symbol\": \"rpm\", \"category\":\"VELOCITY\"}"),
                              DataType.RPM.toJson());
        JsonHelper.assertJson(new JsonObject(
            "{\"type\":\"bool\",\"category\":\"ALL\", \"possible_values\":{\"0.5\":[\"true\",\"on\",\"start\",\"1\"]," +
            "\"0.0\":[\"false\",\"off\",\"stop\",\"0\",\"null\"]}}"), DataType.BOOLEAN.toJson());
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
        JsonHelper.assertJson(possibleValues, JsonObject.mapFrom(dt.possibleValues()));
    }

}
