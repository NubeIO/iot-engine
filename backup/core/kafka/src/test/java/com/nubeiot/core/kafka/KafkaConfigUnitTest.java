package com.nubeiot.core.kafka;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.kafka.supplier.KafkaConsumerProvider;
import com.nubeiot.core.kafka.supplier.KafkaProducerSupplier;
import com.nubeiot.core.utils.Configs;

@RunWith(VertxUnitRunner.class)
public class KafkaConfigUnitTest extends KafkaUnitTestBase {

    @Test
    public void test_can_create_consumer() {
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        KafkaConsumerProvider.create(vertx, from.getConsumerConfig(), String.class, EventMessage.class).close();
    }

    @Test
    public void test_can_create_producer() {
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        KafkaProducerSupplier.create(vertx, from.getProducerConfig(), String.class, EventMessage.class).close();
    }

}
