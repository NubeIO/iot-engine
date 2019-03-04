package com.nubeiot.buildscript.jooq

import org.junit.Assert
import org.junit.Test

class DBTest {

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
