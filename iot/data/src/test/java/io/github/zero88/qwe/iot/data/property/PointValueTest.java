package io.github.zero88.qwe.iot.data.property;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.github.zero88.qwe.JsonHelper;
import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.iot.data.property.PointValue.Fields;
import io.vertx.core.json.JsonObject;

public class PointValueTest {

    @Test
    public void test_serialize() throws JSONException {
        final JsonObject e = new JsonObject().put(Fields.priority, 1).put(Fields.value, "0").put(Fields.rawValue, 0);
        System.out.println(e);
        JsonHelper.assertJson(e, PointValue.builder().priority(1).value("0").build().toJson());
    }

    @Test
    public void test_deserialize_no_priority() {
        final PointValue pv = JsonData.from("{\"value\":\"1\"}", PointValue.class);
        Assert.assertEquals(16, pv.getPriority());
        Assert.assertEquals("1", pv.getValue());
        Assert.assertEquals(Double.valueOf(1.0), pv.getRawValue());
    }

    @Test
    public void test_deserialize_no_raw() {
        final PointValue pv = JsonData.from("{\"priority\":1,\"value\":\"1\"}", PointValue.class);
        Assert.assertEquals(1, pv.getPriority());
        Assert.assertEquals("1", pv.getValue());
        Assert.assertEquals(Double.valueOf(1.0), pv.getRawValue());
    }

    @Test
    public void test_deserialize_no_value() {
        final PointValue pv = JsonData.from("{\"priority\":1,\"rawValue\": 2.0}", PointValue.class);
        Assert.assertEquals(1, pv.getPriority());
        Assert.assertEquals("2.0", pv.getValue());
        Assert.assertEquals(Double.valueOf(2.0), pv.getRawValue());
    }

    @Test
    public void test_deserialize_value_is_raw() {
        final PointValue pv = JsonData.from("{\"priority\":1,\"value\":10}", PointValue.class);
        Assert.assertEquals(1, pv.getPriority());
        Assert.assertEquals("10", pv.getValue());
        Assert.assertEquals(Double.valueOf(10.0), pv.getRawValue());
    }

    @Test
    public void test_deserialize_full() {
        final PointValue pv = JsonData.from("{\"priority\":1,\"value\":\"3\",\"rawValue\":3}", PointValue.class);
        Assert.assertEquals(1, pv.getPriority());
        Assert.assertEquals("3", pv.getValue());
        Assert.assertEquals(Double.valueOf(3.0), pv.getRawValue());
    }

    @Test
    public void test_deserialize_only_value_is_string() {
        final PointValue pv = JsonData.from("{\"priority\":1,\"value\":\"active\"}", PointValue.class);
        Assert.assertEquals(1, pv.getPriority());
        Assert.assertEquals("active", pv.getValue());
        Assert.assertNull(pv.getRawValue());
    }

    @Test
    public void test_deserialize_value_is_string_and_raw_has_value() {
        final PointValue pv = JsonData.from("{\"priority\":1,\"value\":\"on\",\"rawValue\":4}", PointValue.class);
        Assert.assertEquals(1, pv.getPriority());
        Assert.assertEquals("on", pv.getValue());
        Assert.assertEquals(Double.valueOf(4), pv.getRawValue());
    }

    @Test
    public void test_deserialize_from_invalid_json() {
        JsonObject json = new JsonObject().put("a", 1).put("b", 2);
        Assert.assertNull(PointValue.from(json));
    }

    @Test
    public void test_deserialize_from_json() {
        JsonObject json = new JsonObject().put("priority", 10).put("value", 2);
        PointValue pv = PointValue.from(json);
        Assert.assertNotNull(pv);
        Assert.assertEquals(10, pv.getPriority());
        Assert.assertEquals("2", pv.getValue());
        Assert.assertEquals(Double.valueOf(2), pv.getRawValue());
    }

    @Test
    public void test_create_def() {
        PointValue pv = PointValue.createDef();
        Assert.assertNotNull(pv);
        Assert.assertEquals(16, pv.getPriority());
        Assert.assertNull(pv.getValue());
        Assert.assertNull(pv.getRawValue());
    }

}
