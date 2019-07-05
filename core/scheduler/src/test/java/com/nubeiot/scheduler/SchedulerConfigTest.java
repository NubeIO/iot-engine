package com.nubeiot.scheduler;

import org.junit.Assert;
import org.junit.Test;

import com.nubeiot.core.IConfig;

public class SchedulerConfigTest {

    @Test
    public void serialize_default() {
        SchedulerConfig config = new SchedulerConfig("abc");
        System.out.println(config.toJson());
        Assert.assertEquals("abc", config.getSchedulerName());
        Assert.assertEquals("com.nubeiot.scheduler.register.abc", config.getRegisterAddress());
        Assert.assertEquals("com.nubeiot.scheduler.monitor.abc", config.getMonitorAddress());
        Assert.assertNotNull(config.getWorkerConfig());
        Assert.assertEquals("pool-scheduler-abc", config.getWorkerConfig().getPoolName());
        Assert.assertEquals(5, config.getWorkerConfig().getPoolSize());
    }

    @Test
    public void deserialize_default() {
        final SchedulerConfig from = IConfig.from("{\"address\":\"hello\"}", SchedulerConfig.class);
        Assert.assertEquals("hello", from.getRegisterAddress());
    }

}
