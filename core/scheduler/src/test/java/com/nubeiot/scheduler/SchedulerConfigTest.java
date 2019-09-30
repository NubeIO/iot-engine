package com.nubeiot.scheduler;

import java.util.concurrent.TimeUnit;

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
        Assert.assertEquals("worker-pool-scheduler-abc", config.getWorkerConfig().getPoolName());
        Assert.assertEquals(5, config.getWorkerConfig().getPoolSize());
        Assert.assertEquals(60, config.getWorkerConfig().getMaxExecuteTime());
        Assert.assertEquals(TimeUnit.SECONDS, config.getWorkerConfig().getMaxExecuteTimeUnit());
    }

    @Test
    public void deserialize_default() {
        SchedulerConfig from = IConfig.from("{\"schedulerName\":\"hello\"}", SchedulerConfig.class);
        Assert.assertEquals("hello", from.getSchedulerName());
        Assert.assertEquals("com.nubeiot.scheduler.register.hello", from.getRegisterAddress());
        Assert.assertEquals("com.nubeiot.scheduler.monitor.hello", from.getMonitorAddress());
        Assert.assertEquals("worker-pool-scheduler-hello", from.getWorkerConfig().getPoolName());
        Assert.assertEquals(5, from.getWorkerConfig().getPoolSize());
        Assert.assertEquals(60, from.getWorkerConfig().getMaxExecuteTime());
        Assert.assertEquals(TimeUnit.SECONDS, from.getWorkerConfig().getMaxExecuteTimeUnit());
    }

    @Test
    public void deserialize_custom() {
        SchedulerConfig from = IConfig.from(
            "{\"schedulerName\":\"hello\", \"registerAddress\":\"com.nubeiot.scheduler.register.abc\"," +
            "\"monitorAddress\":\"com.nubeiot.scheduler.monitor.abc\"," +
            "\"__schedule_worker__\":{\"poolName\":\"worker-pool-scheduler-abc\"," +
            "\"poolSize\":3,\"maxExecuteTime\":1,\"maxExecuteTimeUnit\":\"MINUTES\"}}", SchedulerConfig.class);
        Assert.assertEquals("hello", from.getSchedulerName());
        Assert.assertEquals("com.nubeiot.scheduler.register.abc", from.getRegisterAddress());
        Assert.assertEquals("com.nubeiot.scheduler.monitor.abc", from.getMonitorAddress());
        Assert.assertEquals("worker-pool-scheduler-abc", from.getWorkerConfig().getPoolName());
        Assert.assertEquals(3, from.getWorkerConfig().getPoolSize());
        Assert.assertEquals(1, from.getWorkerConfig().getMaxExecuteTime());
        Assert.assertEquals(TimeUnit.MINUTES, from.getWorkerConfig().getMaxExecuteTimeUnit());
    }

}
