package com.nubeiot.iotdata.unit;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.TestHelper.JsonHelper;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Functions;

public class UnitDisplayTest {

    @Test
    public void test_valid() {
        Assert.assertEquals("abc", new UnitLabel().add("1", "abc").get("1"));
        Assert.assertEquals("abc", new UnitLabel().add("=1", "abc").get("1"));
        Assert.assertEquals("abc", new UnitLabel().add(">1", "abc").get("> 1"));
        Assert.assertEquals("abc", new UnitLabel().add("<1", "abc").get("< 1"));
        Assert.assertEquals("abc", new UnitLabel().add(">=1", "abc").get(">= 1"));
        Assert.assertEquals("abc", new UnitLabel().add("<=1", "abc").get("<= 1"));
        Assert.assertEquals("abc", new UnitLabel().add("<>1.4", "abc").get("<> 1.4"));
    }

    @Test
    public void test_parse_invalid() {
        Functions.getIfThrow(() -> new UnitLabel().add("", "123"),
                             t -> Assert.assertEquals("Expression cannot be blank", t.getMessage()));
        Functions.getIfThrow(() -> new UnitLabel().add("vv.123", "ttt"), t -> Assert.assertEquals(
            "Expression cannot parse. Only support some operators: =, >, <, >=, <=, <>", t.getMessage()));
        Functions.getIfThrow(() -> new UnitLabel().add("1", ""),
                             t -> Assert.assertEquals("Display value cannot be null", t.getMessage()));
        Functions.getIfThrow(() -> new UnitLabel().add(">=1", "null"),
                             t -> Assert.assertEquals("Display value cannot be null", t.getMessage()));
    }

    @Test
    public void test_serialize() throws JSONException {
        final JsonObject object = new UnitLabel().add("1", "abc")
                                                 .add("=1", "xxx")
                                                 .add(">1", "eee")
                                                 .add("<1", "ddd")
                                                 .add("=5", "xyz")
                                                 .add("<=10", "heh")
                                                 .toJson();
        final JsonObject expected = new JsonObject(
            "{\"= 1.0\":\"abc\",\"= 5.0\":\"xyz\",\"> 1.0\":\"eee\",\"< 1.0\":\"ddd\",\"<= 10.0\":\"heh\"}");
        JsonHelper.assertJson(expected, object);
    }

    @Test
    public void test_deserialize() {
        UnitLabel expected = new UnitLabel().add("=1", "xxx").add(">1", "eee").add("<1", "ddd").add("<=10", "heh");
        UnitLabel parse = JsonData.from(
            new JsonObject("{\"< 1.0\":\"ddd\",\"<= 10.0\":\"heh\",\"> 1.0\":\"eee\",\"= 1.0\":\"xxx\"}"),
            UnitLabel.class);
        Assert.assertEquals(expected, parse);
    }

    @Test
    public void test_merge() throws JSONException {
        UnitLabel d1 = new UnitLabel().add("=1", "xxx").add("<=10", "ho").add("5", "xyz");
        UnitLabel d2 = new UnitLabel().add("=1", "xxx").add(">1", "eee").add("<1", "ddd").add("<=10", "heh");
        final UnitLabel merge = JsonData.merge(d2.toJson(), d1.toJson(), UnitLabel.class);
        final JsonObject expected = new JsonObject(
            "{\"= 1.0\":\"xxx\",\"= 5.0\":\"xyz\",\"> 1.0\":\"eee\",\"< 1.0\":\"ddd\",\"<= 10.0\":\"ho\"}");
        JsonHelper.assertJson(expected, merge.toJson());
    }

    @Test
    public void test_find_label() {
        final UnitLabel display = new UnitLabel().add("1", "abc")
                                                 .add("3", "xxx")
                                                 .add(">1", "eee")
                                                 .add("<1", "ddd")
                                                 .add("<=10", "heh");
        Assert.assertEquals("ddd", display.eval(0));
        Assert.assertEquals("abc", display.eval(1));
        Assert.assertEquals("xxx", display.eval(3));
        Assert.assertEquals("eee", display.eval(2));
        Assert.assertEquals("eee", display.eval(5));
        Assert.assertEquals("heh", display.eval(9));
        Assert.assertEquals("heh", display.eval(10));
        Assert.assertEquals("eee", display.eval(100));
    }

    @Test
    public void test_not_found_label() {
        final UnitLabel display = new UnitLabel().add("1", "abc").add("<1", "ddd").add("<=10", "heh");
        Assert.assertEquals("ddd", display.eval(-10));
        Assert.assertEquals("abc", display.eval(1));
        Assert.assertEquals("heh", display.eval(2));
        Assert.assertEquals("heh", display.eval(10));
        Assert.assertNull(display.eval(11));
    }

}
