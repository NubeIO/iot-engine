package com.nubeiot.core.utils;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

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

    @Test
    public void test_mergeJsonObjectFunc1() throws JSONException {
        JsonObject sourceJsonObject = new JsonObject().put("a", "10").put("d", new JsonArray());
        JsonObject defaultJsonObject = new JsonObject().put("b", 20)
                                                       .put("c", new JsonObject().put("x", "2"))
                                                       .put("d", new JsonArray().add(new JsonObject().put("y", 3)));

        JsonUtils.mergeJsonObjectFunc.apply(sourceJsonObject, defaultJsonObject);
        System.out.println(sourceJsonObject);
        JSONAssert.assertEquals(sourceJsonObject.encode(),
                                "{\"a\":\"10\",\"d\":[{\"y\":3}],\"b\":20," + "\"c\":{\"x\":\"2\"}}", true);
    }

    @Test
    public void test_mergeJsonObjectFunc2() throws JSONException {
        JsonObject sourceJsonObject = new JsonObject().put("a", "10")
                                                      .put("d", new JsonArray().add(new JsonObject().put("z", 3)));
        JsonObject defaultJsonObject = new JsonObject().put("b", 20)
                                                       .put("c", new JsonObject().put("x", "2"))
                                                       .put("d", new JsonArray().add(new JsonObject().put("y", 3)));

        JsonUtils.mergeJsonObjectFunc.apply(sourceJsonObject, defaultJsonObject);
        System.out.println(sourceJsonObject);
        JSONAssert.assertEquals(sourceJsonObject.encode(),
                                "{\"a\":\"10\",\"d\":[{\"z\":3,\"y\":3}],\"b\":20,\"c\":{\"x\":\"2\"}}", true);
    }

    @Test
    public void test_mergeJsonObjectFunc3() throws JSONException {
        JsonObject sourceJsonObject = new JsonObject().put("a", "10").put("d", new JsonArray());
        JsonObject defaultJsonObject = new JsonObject().put("b", 20)
                                                       .put("c", new JsonObject().put("x", "2"))
                                                       .put("d", new JsonArray().add(new JsonArray().add("Hello")));

        JsonUtils.mergeJsonObjectFunc.apply(sourceJsonObject, defaultJsonObject);
        System.out.println(sourceJsonObject);
        JSONAssert.assertEquals(sourceJsonObject.encode(),
                                "{\"a\":\"10\",\"d\":[[\"Hello\"]],\"b\":20,\"c\":{\"x\":\"2\"}}", true);
    }

    @Test
    public void test_mergeJsonObjectFunc4() throws JSONException {
        JsonObject sourceJsonObject = new JsonObject().put("a", "10");
        JsonObject defaultJsonObject = new JsonObject().put("b", 20)
                                                       .put("c", new JsonObject().put("x", "2"))
                                                       .put("d", new JsonArray().add("y"));

        JsonUtils.mergeJsonObjectFunc.apply(sourceJsonObject, defaultJsonObject);
        System.out.println(sourceJsonObject);
        JSONAssert.assertEquals(sourceJsonObject.encode(), "{\"a\":\"10\",\"b\":20,\"c\":{\"x\":\"2\"},\"d\":[\"y\"]}",
                                true);
    }

    @Test
    public void test_mergeJsonObjectFunc5() throws JSONException {
        JsonObject sourceJsonObject = new JsonObject().put("d",
                                                           new JsonArray().add(new JsonObject()).add(new JsonObject()));
        JsonObject defaultJsonObject = new JsonObject().put("d", new JsonArray().add(
            new JsonObject().put("x", new JsonArray().add("2"))));

        JsonUtils.mergeJsonObjectFunc.apply(sourceJsonObject, defaultJsonObject);
        System.out.println(sourceJsonObject);
        JSONAssert.assertEquals(sourceJsonObject.encode(), "{\"d\":[{\"x\":[\"2\"]},{\"x\":[\"2\"]}]}", true);
    }

}
