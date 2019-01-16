package com.nubeiot.core.utils;

import org.junit.Assert;
import org.junit.Test;

public class SQLUtilsTest {

    @Test
    public void testIn() {
        Assert.assertTrue(SQLUtils.in("abc", "abc"));
        Assert.assertFalse(SQLUtils.in("abc", "Abc"));
        Assert.assertTrue(SQLUtils.in("abc", "xyz", "abc"));
        Assert.assertFalse(SQLUtils.in("abc", "xyz", "uvw"));
        Assert.assertFalse(SQLUtils.in(null, "xyz", "uvw"));
        Assert.assertFalse(SQLUtils.in(null, null, "uvw"));

        Assert.assertTrue(SQLUtils.in("abc", true, "Abc"));
        Assert.assertTrue(SQLUtils.in("abc", true, "xyz", "ABC"));
    }

}
