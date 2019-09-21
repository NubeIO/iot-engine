package com.nubeiot.core.kafka.service;

import java.util.Collection;
import java.util.Map;

import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.kafka.KafkaConfig.ProducerCfg;
import com.nubeiot.core.kafka.KafkaRouter;
import com.nubeiot.core.transport.Transporter;

public interface KafkaProducerService extends Transporter {

    static KafkaProducerService create(Vertx vertx, ProducerCfg config, KafkaRouter router, String sharedKey) {
        return new ProducerService(vertx, config, router.getProducerTechId(),
                                   router.getProducerExceptionHandler()).create(sharedKey, router.getProducerEvents());
    }

    <K, V> KafkaProducer<K, V> producer(String topic);

    Collection<KafkaProducer> producers();

    default <V> void publish(String topic, V value) {
        this.publish(EventAction.CREATE, topic, value);
    }

    default <V> void publish(EventAction action, String topic, V value) {
        this.publish(action, topic, null, value);
    }

    default <K, V> void publish(String topic, K key, V value) {
        this.publish(EventAction.CREATE, topic, key, value);
    }

    default <K, V> void publish(EventAction action, String topic, K key, V value) {
        this.publish(action, topic, null, key, value);
    }

    default <K, V> void publish(String topic, Integer partition, K key, V value) {
        this.publish(EventAction.CREATE, topic, partition, key, value);
    }

    default <K, V> void publish(EventAction action, String topic, Integer partition, K key, V value) {
        this.publish(action, topic, partition, key, value, null);
    }

    default <K, V> void publish(EventAction action, String topic, Integer partition, K key, V value,
                                Map<String, Object> headers) {
        this.publish(EventMessage.initial(action), topic, partition, key, value, headers);
    }

    default <K, V> void publish(EventMessage message, String topic, Integer partition, K key, V value) {
        this.publish(message, topic, partition, key, value, null);
    }

    <K, V> void publish(EventMessage message, String topic, Integer partition, K key, V value,
                        Map<String, Object> headers);

}
