package com.nubeiot.core.kafka;

import java.util.Collection;
import java.util.Collections;
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
import com.nubeiot.core.kafka.handler.producer.KafkaProducerHandler;
import com.nubeiot.core.kafka.handler.producer.LogKafkaProducerHandler;
import com.nubeiot.core.kafka.supplier.KafkaProducerSupplier;
import com.nubeiot.core.utils.DateTimes;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public final class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final Vertx vertx;
    private final ProducerCfg config;
    private final Map<String, ClientTechId> techIdMap;
    private final Map<String, KafkaProducer> producers = new HashMap<>();
    private final Map<String, KafkaProducerHandler> handlers = new HashMap<>();

    static KafkaProducerService create(Vertx vertx, ProducerCfg config, KafkaRouter router) {
        return new KafkaProducerService(vertx, config, router.getProducerTechId()).create(router.getProducerEvents());
    }

    public <K, V> KafkaProducer<K, V> producer(String topic) {
        return producers.get(topic);
    }

    Collection<KafkaProducer> producers() {
        return Collections.unmodifiableCollection(producers.values());
    }

    public <V> void publish(String topic, V value) {
        this.publish(topic, null, null, value);
    }

    public <K, V> void publish(String topic, K key, V value) {
        this.publish(topic, null, key, value);
    }

    public <K, V> void publish(String topic, Integer partition, K key, V value) {
        validate(topic, key, value).write(
            KafkaProducerRecord.create(topic, key, value, DateTimes.now().toInstant().toEpochMilli(), partition),
            handlers.get(topic));
    }

    private <K, V> KafkaProducer validate(String topic, K key, V value) {
        ClientTechId clientTechId = techIdMap.get(topic);
        if (Objects.isNull(clientTechId)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Topic " + topic + " is not yet registered");
        }
        if (Objects.nonNull(key) && !clientTechId.getKeyClass().isInstance(key)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT,
                                    "Topic " + topic + " is registered with different key type " +
                                    clientTechId.getKeyClass());
        }
        if (Objects.nonNull(value) && !clientTechId.getValueClass().isInstance(value)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT,
                                    "Topic " + topic + " is registered with different value type " +
                                    clientTechId.getValueClass());
        }
        return producers.get(topic);
    }

    private KafkaProducerService create(Set<KafkaEventMetadata> producerEvents) {
        Map<ClientTechId, KafkaProducer> temp = new HashMap<>();
        producerEvents.forEach(event -> {
            KafkaProducerHandler producerHandler = Objects.isNull(event.getProducerHandler())
                                                   ? LogKafkaProducerHandler.DEFAULT
                                                   : event.getProducerHandler();
            this.producers.put(event.getTopic(), temp.computeIfAbsent(event.getTechId(), this::create));
            this.handlers.put(event.getTopic(), producerHandler);
            logger.info("Registering Kafka Producer | Topic: {} | Kind: {} | Handler: {}", event.getTopic(),
                        event.getTechId(), producerHandler.getClass().getName());
        });
        logger.debug("Registered {} kind of Kafka Producer", temp.size());
        return this;
    }

    private KafkaProducer create(ClientTechId techId) {
        return KafkaProducerSupplier.create(vertx, config, techId.getKeySerdes().serializer(),
                                            techId.getValueSerdes().serializer())
                                    .exceptionHandler(
                                        t -> logger.error("Error occurs in Kafka Producer with techId {}", t, techId));
    }

}
