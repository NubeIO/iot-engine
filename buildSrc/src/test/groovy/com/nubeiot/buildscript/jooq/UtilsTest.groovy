package com.nubeiot.buildscript.jooq

import org.junit.Assert
import org.junit.Test

import com.nubeiot.buildscript.jooq.Utils

class UtilsTest {

    @Test
    void snakeCase_upper() {
        Assert.assertEquals("TEST", Utils.toSnakeCase("test"))
        Assert.assertEquals("TEST", Utils.toSnakeCase("Test"))
        Assert.assertEquals("TEST", Utils.toSnakeCase("TEST"))
        Assert.assertEquals("TEST_ABC", Utils.toSnakeCase("test_abc"))
        Assert.assertEquals("TEST_ABC", Utils.toSnakeCase("testAbc"))
        Assert.assertEquals("TEST_ABC", Utils.toSnakeCase("TestAbc"))
        Assert.assertEquals("TEST_ABC", Utils.toSnakeCase("TEST_ABC"))
        Assert.assertEquals("TEST_ABC", Utils.toSnakeCase("test_abc"))
    }

    @Test
    void snakeCase_lower() {
        Assert.assertEquals("test", Utils.toSnakeCase("test", false))
        Assert.assertEquals("t_e_s_t", Utils.toSnakeCase("Test", false))
        Assert.assertEquals("test", Utils.toSnakeCase("TEST", false))
        Assert.assertEquals("test_abc", Utils.toSnakeCase("TEST_ABC", false))
        Assert.assertEquals("test_abc", Utils.toSnakeCase("TESTaBC", false))
        Assert.assertEquals("test_a_b_c", Utils.toSnakeCase("TEST_Abc", false))
        Assert.assertEquals("test_abc", Utils.toSnakeCase("test_abc", false))
    }

    @Test
    void replace_json_suffix() {
        Assert.assertEquals("abc", Utils.replaceJsonSuffix("abc"))
        Assert.assertEquals("abc", Utils.replaceJsonSuffix("abc_json"))
        Assert.assertEquals("abc", Utils.replaceJsonSuffix("abc_json_array"))
        Assert.assertEquals("abc", Utils.replaceJsonSuffix("abc_array"))
        Assert.assertEquals("ABC", Utils.replaceJsonSuffix("ABC_JSON"))
        Assert.assertEquals("ABC", Utils.replaceJsonSuffix("ABC_JSON_ARRAY"))
        Assert.assertEquals("ABC", Utils.replaceJsonSuffix("ABC_ARRAY"))
    }

    @Test
    void expression_datetime_zone() {
        Assert.assertFalse("abc".matches(DB.TYPES.timestampz))
        Assert.assertFalse("TIMESTAMP".matches(DB.TYPES.timestampz))
        Assert.assertFalse("TIMESTAMP WITH TIME ZONE1".matches(DB.TYPES.timestampz))
        Assert.assertTrue("TIMESTAMP WITH TIME ZONE".matches(DB.TYPES.timestampz))
        Assert.assertTrue("timestamp with time zone".matches(DB.TYPES.timestampz))
        Assert.assertTrue("timestamp(6) with time zone".matches(DB.TYPES.timestampz))
        Assert.assertFalse("timestamp with time zone(9)".matches(DB.TYPES.timestampz))
    }
}
