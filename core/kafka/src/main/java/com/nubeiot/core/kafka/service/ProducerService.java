package com.nubeiot.core.kafka.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.kafka.ClientTechId;
import com.nubeiot.core.kafka.KafkaConfig.ProducerCfg;
import com.nubeiot.core.kafka.KafkaEventMetadata;
import com.nubeiot.core.kafka.handler.KafkaErrorHandler;
import com.nubeiot.core.kafka.handler.producer.KafkaProducerHandler;
import com.nubeiot.core.kafka.supplier.KafkaProducerSupplier;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class ProducerService implements KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final Vertx vertx;
    private final ProducerCfg config;
    private final Map<String, ClientTechId> techIdMap;
    private final Map<ClientTechId, KafkaErrorHandler> errorHandlers;
    private final Map<String, KafkaProducer> producers = new HashMap<>();
    private final Map<String, KafkaProducerHandler> handlers = new HashMap<>();

    public <K, V> KafkaProducer<K, V> producer(String topic) {
        return producers.get(topic);
    }

    public Collection<KafkaProducer> producers() {
        return Collections.unmodifiableCollection(producers.values());
    }

    @Override
    public <K, V> void publish(EventMessage message, String topic, Integer partition, K key, V value,
                               Map<String, Object> headers) {
        KafkaProducer producer = validate(topic, key, value);
        KafkaProducerHandler handler = handlers.get(topic);
        KafkaProducerRecord<K, V> record = handler.transformer().apply(message, topic, partition, key, value, null);
        logger.info("Kafka producer sending data...");
        if (logger.isTraceEnabled()) {
            logger.trace("Kafka producer record: {}", record.toString());
        }
        producer.write(record, handler);
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

    ProducerService create(Function<String, Object> sharedDataFunc, Set<KafkaEventMetadata> producerEvents) {
        Map<ClientTechId, KafkaProducer> temp = new HashMap<>();
        producerEvents.forEach(event -> {
            KafkaProducerHandler handler = Objects.isNull(event.getProducerHandler())
                                           ? KafkaProducerHandler.DEFAULT
                                           : event.getProducerHandler();
            handler.registerTransformer(event.getProducerTransformer()).registerSharedData(sharedDataFunc);
            this.producers.put(event.getTopic(), temp.computeIfAbsent(event.getTechId(), this::create));
            this.handlers.put(event.getTopic(), handler);
            logger.info("Registering Kafka Producer | Topic: {} | Kind: {} | Handler: {} | Transformer: {}",
                        event.getTopic(), event.getTechId(), handler.getClass().getName(),
                        handler.transformer().getClass().getName());
        });
        logger.debug("Registered {} kind of Kafka Producer", temp.size());
        return this;
    }

    private KafkaProducer create(ClientTechId techId) {
        KafkaProducer producer = KafkaProducerSupplier.create(vertx, config, techId.getKeySerdes().serializer(),
                                                              techId.getValueSerdes().serializer());
        return producer.exceptionHandler(
            t -> errorHandlers.getOrDefault(techId, KafkaErrorHandler.PRODUCER_ERROR_HANDLER)
                              .accept(techId, config.getClientId(), (Throwable) t));
    }

}
