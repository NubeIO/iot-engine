package com.nubeiot.core.kafka;

import org.junit.Test;

public class KafkaConfigTest {

    @Test
    public void test_default() {
        System.out.println(new KafkaConfig().toJson().encodePrettily());
    }

}
