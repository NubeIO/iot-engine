package com.nubeiot.scheduler;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.IConfig;

public class SchedulerConfigTest {

    @Test
    public void serialize_default() {
        SchedulerConfig config = new SchedulerConfig();
        System.out.println(config.toJson());
        Assert.assertTrue(config.getAddress().startsWith("com.nubeiot.scheduler."));
    }

    @Test
    public void deserialize_default() {
        final SchedulerConfig from = IConfig.from("{\"address\":\"hello\"}", SchedulerConfig.class);
        Assert.assertEquals("hello", from.getAddress());
    }

}
