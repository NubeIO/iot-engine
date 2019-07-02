package com.nubeiot.scheduler;

import org.junit.Test;

public class SchedulerConfigTest {

    @Test
    public void serialize() {
        System.out.println(new SchedulerConfig().toJson());
    }

}
