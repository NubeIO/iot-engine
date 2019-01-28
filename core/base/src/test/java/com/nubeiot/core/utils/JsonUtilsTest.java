package com.nubeiot.core.utils;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonUtilsTest {

    @Test
    public void test_get_json_value() {
        JsonObject jsonObject = new JsonObject().put("details", new JsonObject().put("first_name", "Shane")
                                                                                .put("last_name", "Watson")
                                                                                .put("club", new JsonArray().add(
                                                                                    "Sydney Sixers").add("RR")));
        Assert.assertEquals(JsonUtils.getObject(jsonObject, "details").toString(),
                            "{\"first_name\":\"Shane\"," + "\"last_name\":\"Watson\",\"club\":[\"Sydney " +
                            "Sixers\",\"RR\"]}");
        Assert.assertEquals(JsonUtils.getObject(jsonObject, "details.first_name"), "Shane");
        Assert.assertEquals(JsonUtils.getObject(jsonObject, "details.last_name"), "Watson");
        Assert.assertEquals(JsonUtils.getObject(jsonObject, "details.club").toString(), "[\"Sydney Sixers\",\"RR\"]");

        Assert.assertNull(JsonUtils.getObject(jsonObject, "details.last_name.club"));
    }

    @Test
    public void test_compareJsonObject_success() {
        JsonObject actual = new JsonObject().put("details", new JsonObject().put("first_name", "Shane"));
        JsonObject expected = new JsonObject().put("details", new JsonObject().put("first_name", "Shane"));

        Assert.assertTrue(JsonUtils.compareJsonObject(expected, actual));
        Assert.assertTrue(JsonUtils.compareJsonObject(null, null));
    }

    @Test
    public void test_compareJsonObject_failure() {
        JsonObject actual = new JsonObject().put("details", new JsonObject().put("first_name", "Shane"));
        JsonObject expected = new JsonObject().put("details", new JsonObject());

        Assert.assertFalse(JsonUtils.compareJsonObject(expected, actual));
        Assert.assertFalse(JsonUtils.compareJsonObject(expected, null));
        Assert.assertFalse(JsonUtils.compareJsonObject(null, actual));
    }

}
