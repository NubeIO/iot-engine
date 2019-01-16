package com.nubeiot.core.kafka;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.kafka.KafkaConfig.ProducerCfg;
import com.nubeiot.core.kafka.supplier.KafkaProducerSupplier;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @NonNull
    private final Vertx vertx;
    @NonNull
    private final ProducerCfg config;
    @NonNull
    private final Map<String, ClientTechId> techIdMap;
    private final Map<String, KafkaProducer> producers = new HashMap<>();

    static KafkaProducerService create(Vertx vertx, ProducerCfg config, KafkaRouter router) {
        return new KafkaProducerService(vertx, config, router.getProducerTechId()).create(router.getProducerEvents());
    }

    public <K, V> KafkaProducer<K, V> producer(String topic) {
        return producers.get(topic);
    }

    Collection<KafkaProducer> producers() {
        return producers.values();
    }

    public <V> void publish(String topic, V value) {
        this.publish(topic, null, null, value);
    }

    public <K, V> void publish(String topic, K key, V value) {
        this.publish(topic, null, key, value);
    }

    public <K, V> void publish(String topic, Integer partition, K key, V value) {
        ClientTechId clientTechId = techIdMap.get(topic);
        if (Objects.isNull(clientTechId)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Topic " + topic + " is not yet registered");
        }
        if (Objects.nonNull(key) && !clientTechId.getKeyClass().isInstance(key)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Topic " + topic + " is not yet registered");
        }
        if (Objects.nonNull(value) && !clientTechId.getValueClass().isInstance(value)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Topic " + topic + " is not yet registered");
        }
        producers.get(topic).write(KafkaProducerRecord.create(topic, key, value, partition));
    }

    private KafkaProducerService create(Set<KafkaEventMetadata> producerEvents) {
        producerEvents.forEach(
            event -> this.producers.put(event.getTopic(), create(event.getTopic(), event.getTechId())));
        return this;
    }

    private <K, V> KafkaProducer create(String topic, ClientTechId techId) {
        if (Objects.nonNull(this.techIdMap.get(topic))) {
            return this.producers.get(topic);
        }
        KafkaProducer<K, V> producer = KafkaProducerSupplier.create(vertx, config, techId.getKeySerdes().serializer(),
                                                                    techId.getValueSerdes().serializer());
        producer.exceptionHandler(t -> logger.error("Error occurs in Kafka Producer", t));
        return producer;
    }

}
