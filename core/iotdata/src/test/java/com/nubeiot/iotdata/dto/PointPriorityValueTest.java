package com.nubeiot.iotdata.dto;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.NubeException;

public class PointPriorityValueTest {

    private PointPriorityValue pv;

    @Before
    public void setup() {
        pv = new PointPriorityValue().add(8).add(9, 9.0).add(1, 3.5).add(10, 10.5);
    }

    @Test
    public void test_serialize() throws JSONException {
        JsonHelper.assertJson(new JsonObject("{\"1\":3.5,\"8\":8.0,\"9\":9.0,\"10\":10.5}"), pv.toJson());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_add_invalid() {
        pv.add(18, 4);
    }

    @Test
    public void test_deserialize() throws JSONException {
        PointPriorityValue from = JsonData.from("{\"1\":3.5,\"8\":8.0,\"9\":9.0,\"10\":10.5}",
                                                PointPriorityValue.class);
        JsonHelper.assertJson(new JsonObject("{\"1\":3.5,\"8\":8.0,\"9\":9.0,\"10\":10.5}"), from.toJson());
        Assert.assertEquals(pv, from);
        Double aDouble = from.get(9);
        Double expected = 9.0d;
        Assert.assertEquals(aDouble, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_deserialize_invalid_priority() throws Throwable {
        try {
            JsonData.from(new JsonObject("{\"90\":3.5}"), PointPriorityValue.class);
        } catch (NubeException e) {
            final Throwable rootCause = e.getCause().getCause().getCause().getCause();
            Assert.assertEquals("Priority is only in range [1, 17]", rootCause.getMessage());
            throw rootCause;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_deserialize_invalid_value() throws Throwable {
        try {
            JsonData.from(new JsonObject("{\"12\":\"444.5s\"}"), PointPriorityValue.class);
        } catch (NubeException e) {
            final Throwable rootCause = e.getCause().getCause().getCause().getCause();
            Assert.assertEquals("Value must be number", rootCause.getMessage());
            throw rootCause;
        }
    }

    @Test
    public void test_merge() throws Throwable {
        final PointPriorityValue merge = JsonData.merge(pv.toJson(), new JsonObject(
            "{\"9\":55,\"10\":14, \"11\":null,\"12\":\"444.5\"}"), PointPriorityValue.class);
        JsonHelper.assertJson(new JsonObject("{\"1\":3.5,\"8\":8.0,\"9\":55.0,\"10\":14.0,\"11\":null,\"12\":444.5}"),
                              merge.toJson());
    }

}
