package com.nubeiot.buildscript

import org.junit.Assert
import org.junit.Test

class StringsTest {
    @Test
    void snakeCase_upper() {
        Assert.assertEquals("TEST", Strings.toSnakeCase("test"))
        Assert.assertEquals("TEST", Strings.toSnakeCase("Test"))
        Assert.assertEquals("TEST", Strings.toSnakeCase("TEST"))
        Assert.assertEquals("TEST_ABC", Strings.toSnakeCase("test_abc"))
        Assert.assertEquals("TEST_ABC", Strings.toSnakeCase("testAbc"))
        Assert.assertEquals("TEST_ABC", Strings.toSnakeCase("TestAbc"))
        Assert.assertEquals("TEST_ABC", Strings.toSnakeCase("TEST_ABC"))
        Assert.assertEquals("TEST_ABC", Strings.toSnakeCase("test_abc"))
    }

    @Test
    void snakeCase_lower() {
        Assert.assertEquals("test", Strings.toSnakeCase("test", false))
        Assert.assertEquals("t_e_s_t", Strings.toSnakeCase("Test", false))
        Assert.assertEquals("test", Strings.toSnakeCase("TEST", false))
        Assert.assertEquals("test_abc", Strings.toSnakeCase("TEST_ABC", false))
        Assert.assertEquals("test_abc", Strings.toSnakeCase("TESTaBC", false))
        Assert.assertEquals("test_a_b_c", Strings.toSnakeCase("TEST_Abc", false))
        Assert.assertEquals("test_abc", Strings.toSnakeCase("test_abc", false))
    }

    @Test
    void replace_json_suffix() {
        Assert.assertEquals("abc", Strings.replaceJsonSuffix("abc"))
        Assert.assertEquals("abc", Strings.replaceJsonSuffix("abc_json"))
        Assert.assertEquals("abc", Strings.replaceJsonSuffix("abc_json_array"))
        Assert.assertEquals("abc", Strings.replaceJsonSuffix("abc_array"))
        Assert.assertEquals("ABC", Strings.replaceJsonSuffix("ABC_JSON"))
        Assert.assertEquals("ABC", Strings.replaceJsonSuffix("ABC_JSON_ARRAY"))
        Assert.assertEquals("ABC", Strings.replaceJsonSuffix("ABC_ARRAY"))
    }
}
