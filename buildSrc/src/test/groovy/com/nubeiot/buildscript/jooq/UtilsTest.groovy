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
}
