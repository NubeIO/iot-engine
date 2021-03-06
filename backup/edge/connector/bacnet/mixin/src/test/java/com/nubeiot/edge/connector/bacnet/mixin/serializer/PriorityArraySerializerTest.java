package com.nubeiot.edge.connector.bacnet.mixin.serializer;

import org.json.JSONException;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.edge.connector.bacnet.mixin.BACnetMixin;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.serotonin.bacnet4j.enums.DayOfWeek;
import com.serotonin.bacnet4j.enums.Month;
import com.serotonin.bacnet4j.type.constructed.LimitEnable;
import com.serotonin.bacnet4j.type.constructed.PriorityArray;
import com.serotonin.bacnet4j.type.constructed.PriorityValue;
import com.serotonin.bacnet4j.type.enumerated.AbortReason;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Date;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.Time;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class PriorityArraySerializerTest {

    @Test
    public void test_serialize_priority_array() throws JSONException {
        final PriorityArray array = new PriorityArray();
        array.put(1, new PriorityValue(new CharacterString("xxx")));
        array.put(2, new PriorityValue(new UnsignedInteger(10)));
        array.put(3, new PriorityValue(AbortReason.other));
        array.put(4, new PriorityValue(Boolean.TRUE));
        array.put(5, new PriorityValue(new Time(10, 5, 10, 0)));
        array.put(6, new PriorityValue(new Date(2020, Month.JANUARY, 21, DayOfWeek.TUESDAY)));
        array.put(7, new PriorityValue(new LimitEnable(true, false)));
        array.put(8, new PriorityValue(Null.instance));
        final JsonObject expected = new JsonObject(
            "{\"1\":\"xxx\",\"2\":10,\"3\":\"other\",\"4\":true,\"5\":\"10:05:10Z\",\"6\":\"2020-01-21Z\"," +
            "\"7\":{\"low-limit-enable\":true,\"high-limit-enable\":false},\"8\":null,\"9\":null,\"10\":null," +
            "\"11\":null,\"12\":null,\"13\":null,\"14\":null,\"15\":null,\"16\":null}");
        final JsonObject json = BACnetMixin.MAPPER.convertValue(array, JsonObject.class);
        JsonHelper.assertJson(expected, json);
    }

    @Test
    public void test_deserialize_priority_array() throws JSONException {
        final JsonObject query = new JsonObject("{\"priority-array\":{\"1\":\"xxx\"}}");
        final PropertyValuesMixin mixin = JsonData.convert(query, PropertyValuesMixin.class, BACnetMixin.MAPPER);
        final JsonObject expected = new JsonObject(
            "{\"priority-array\":{\"1\":\"xxx\",\"2\":null,\"3\":null,\"4\":null,\"5\":null,\"6\":null,\"7\":null," +
            "\"8\":null,\"9\":null,\"10\":null,\"11\":null,\"12\":null,\"13\":null,\"14\":null,\"15\":null," +
            "\"16\":null}}");
        JsonHelper.assertJson(expected, mixin.toJson());
    }

}
