package com.nubeiot.core.utils;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

public class SQLUtilsTest {

    @Test
    public void test_in() {
        Assert.assertTrue(SQLUtils.in("abc", "abc"));
        Assert.assertFalse(SQLUtils.in("abc", "Abc"));
        Assert.assertTrue(SQLUtils.in("abc", "xyz", "abc"));
        Assert.assertFalse(SQLUtils.in("abc", "xyz", "uvw"));
        Assert.assertFalse(SQLUtils.in(null, "xyz", "uvw"));
        Assert.assertFalse(SQLUtils.in(null, null, "uvw"));

        Assert.assertTrue(SQLUtils.in("abc", true, "Abc"));
        Assert.assertTrue(SQLUtils.in("abc", true, "xyz", "ABC"));
    }

    @Test
    public void test_getMatchValueOrFirstOne_does_not_match_happyCase() {
        String[] values = new String[] {"Shane", "Michel"};
        String output = SQLUtils.getMatchValueOrFirstOne("1", values);
        Assert.assertEquals("Shane", output);
    }

    @Test
    public void test_getFirstNotNull() {
        Assert.assertEquals("Shane", SQLUtils.getFirstNotNull("", "Shane", "Watson"));
        JsonObject object = new JsonObject().put("first_name", "Shane");
        Assert.assertEquals(object, SQLUtils.getFirstNotNull(null, object));
    }

    @Test
    public void test_getMatchValueOrFirstOne_matched_happyCase() {
        String[] values = new String[] {"Shane", "Michel"};
        String output = SQLUtils.getMatchValueOrFirstOne("Michel", values);
        Assert.assertEquals("Michel", output);
    }

    @Test
    public void test_getMatchValueOrFirstOne_nullCase() {
        String[] values = new String[] {};
        String output = SQLUtils.getMatchValueOrFirstOne("Michel", values);
        Assert.assertNull(output);
    }

}
