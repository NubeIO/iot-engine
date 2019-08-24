package com.nubeiot.iotdata.unit;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.iotdata.unit.DataTypeCategory.All;
import com.nubeiot.iotdata.unit.DataTypeCategory.AngularVelocity;
import com.nubeiot.iotdata.unit.DataTypeCategory.ElectricPotential;
import com.nubeiot.iotdata.unit.DataTypeCategory.Illumination;
import com.nubeiot.iotdata.unit.DataTypeCategory.Power;
import com.nubeiot.iotdata.unit.DataTypeCategory.Pressure;
import com.nubeiot.iotdata.unit.DataTypeCategory.Temperature;

public class DataTypeTest {

    @Test
    public void test_serialize_dataType() throws JSONException {
        JsonHelper.assertJson(new JsonObject("{\"type\":\"number\", \"category\":\"ALL\"}"), All.NUMBER.toJson());
        JsonHelper.assertJson(new JsonObject("{\"type\":\"percentage\", \"symbol\": \"%\", \"category\":\"ALL\"}"),
                              All.PERCENTAGE.toJson());
        JsonHelper.assertJson(
            new JsonObject("{\"type\":\"celsius\", \"symbol\": \"°C\", \"category\":\"TEMPERATURE\"}"),
            Temperature.CELSIUS.toJson());
        JsonHelper.assertJson(
            new JsonObject("{\"type\":\"volt\", \"symbol\": \"V\", \"category\":\"ELECTRIC_POTENTIAL\"}"),
            ElectricPotential.VOLTAGE.toJson());
        JsonHelper.assertJson(new JsonObject("{\"type\":\"dBm\", \"symbol\": \"dBm\", \"category\":\"POWER\"}"),
                              Power.DBM.toJson());
        JsonHelper.assertJson(
            new JsonObject("{\"type\":\"hectopascal\", \"symbol\": \"hPa\", \"category\":\"PRESSURE\"}"),
            Pressure.HPA.toJson());
        JsonHelper.assertJson(new JsonObject("{\"type\":\"lux\", \"symbol\": \"lx\", \"category\":\"ILLUMINATION\"}"),
                              Illumination.LUX.toJson());
        JsonHelper.assertJson(
            new JsonObject("{\"type\":\"kilowatt_hour\", \"symbol\": \"kWh\", \"category\":\"POWER\"}"),
            Power.KWH.toJson());
        JsonHelper.assertJson(
            new JsonObject("{\"type\":\"revolutions_per_minute\", \"symbol\": \"rpm\", \"category\":\"VELOCITY\"}"),
            AngularVelocity.RPM.toJson());
        JsonHelper.assertJson(new JsonObject("{\"type\":\"bool\",\"category\":\"ALL\"}"), All.BOOLEAN.toJson());
    }

    @Test
    public void test_deserialize_numberType() {
        assertNumberDataType("{\"type\":\"number\"}", "number", null);
        assertNumberDataType("{\"type\":\"percentage\"}", "percentage", "%");
        assertNumberDataType("{\"type\":\"celsius\"}", "celsius", "°C");
        assertNumberDataType("{\"type\":\"volt\"}", "volt", "V");
        assertNumberDataType("{\"type\":\"dBm\"}", "dBm", "dBm");
        assertNumberDataType("{\"type\":\"hectopascal\"}", "hectopascal", "hPa");
        assertNumberDataType("{\"type\":\"lux\"}", "lux", "lx");
        assertNumberDataType("{\"type\":\"kilowatt_hour\"}", "kilowatt_hour", "kWh");
        assertNumberDataType("{\"type\":\"unknown\", \"symbol\": \"xxx\"}", "unknown", "xxx");
    }

    private void assertNumberDataType(String from, String type, String unit) {
        final DataType dt = JsonData.from(from, DataType.class);
        Assert.assertTrue(dt instanceof NumberDataType);
        Assert.assertEquals(type, dt.type());
        Assert.assertEquals(unit, dt.unit());
        Assert.assertNull(dt.label());
    }

    @Test
    public void test_deserialize_booleanType() {
        final DataType dt = JsonData.from("{\"type\":\"bool\"}", DataType.class);
        Assert.assertTrue(dt instanceof NumberDataType);
        Assert.assertEquals("bool", dt.type());
        Assert.assertNull(dt.unit());
    }

}
