package com.nubeiot.core.kafka.service;

import java.util.Collection;
import java.util.function.Function;

import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;

import com.nubeiot.core.kafka.KafkaConfig.ConsumerCfg;
import com.nubeiot.core.kafka.KafkaRouter;

public interface KafkaConsumerService {

    static KafkaConsumerService create(Vertx vertx, ConsumerCfg config, KafkaRouter router,
                                       Function<String, Object> sharedDataFunc) {
        return new ConsumerService(vertx, config, router.getConsumerTechId(),
                                   router.getConsumerExceptionHandler()).create(sharedDataFunc,
                                                                                router.getConsumerEvents());
    }

    <K, V> KafkaConsumer<K, V> consumer(String topic);

    Collection<KafkaConsumer> consumers();

}
