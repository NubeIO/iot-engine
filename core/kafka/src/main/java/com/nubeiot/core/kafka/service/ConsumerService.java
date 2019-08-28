package com.nubeiot.core.kafka.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumer;

import com.nubeiot.core.kafka.ClientTechId;
import com.nubeiot.core.kafka.KafkaConfig.ConsumerCfg;
import com.nubeiot.core.kafka.KafkaEventMetadata;
import com.nubeiot.core.kafka.handler.KafkaErrorHandler;
import com.nubeiot.core.kafka.handler.consumer.ConsumerDispatcher;
import com.nubeiot.core.kafka.handler.consumer.KafkaConsumerHandler;
import com.nubeiot.core.kafka.supplier.KafkaConsumerProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class ConsumerService implements KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final Vertx vertx;
    private final ConsumerCfg config;
    private final Map<String, ClientTechId> techIdMap;
    private final Map<ClientTechId, KafkaErrorHandler> errorHandlers;
    private final Map<String, KafkaConsumer> consumers = new HashMap<>();

    public KafkaConsumer consumer(String topic) {
        return consumers.get(topic);
    }

    public Collection<KafkaConsumer> consumers() {
        return Collections.unmodifiableCollection(consumers.values());
    }

    ConsumerService create(String sharedKey, Map<ClientTechId, Set<KafkaEventMetadata>> consumerEvents) {
        Map<ClientTechId, KafkaConsumer> temp = new HashMap<>();
        techIdMap.forEach((topic, techId) -> consumers.put(topic, temp.computeIfAbsent(techId, this::create)));
        consumerEvents.forEach((techId, metadata) -> registerHandler(temp.get(techId), sharedKey, metadata));
        logger.debug("Registered {} Kafka Consumer(s) successfully", temp.size());
        return this;
    }

    private <K, V> void registerHandler(KafkaConsumer<K, V> consumer, String sharedKey,
                                        Set<KafkaEventMetadata> identicalTechIdEvents) {
        ConsumerDispatcher<K, V> dispatcher = createDispatcher(sharedKey, identicalTechIdEvents);
        consumer.handler(dispatcher::accept).subscribe(dispatcher.topics());
    }

    private <K, V> KafkaConsumer<K, V> create(ClientTechId techId) {
        KafkaConsumer consumer = KafkaConsumerProvider.create(vertx, config, techId.getKeySerdes().deserializer(),
                                                              techId.getValueSerdes().deserializer());
        return consumer.exceptionHandler(
            t -> errorHandlers.getOrDefault(techId, KafkaErrorHandler.CONSUMER_ERROR_HANDLER)
                              .accept(techId, config.getClientId(), (Throwable) t));
    }

    private <K, V> ConsumerDispatcher<K, V> createDispatcher(String sharedKey,
                                                             Set<KafkaEventMetadata> identicalTechIdEvents) {
        ConsumerDispatcher.Builder<K, V> builder = ConsumerDispatcher.builder();
        identicalTechIdEvents.forEach(event -> {
            KafkaConsumerHandler handler = createConsumerHandler(sharedKey, event);
            builder.handler(event.getTopic(), handler);
            logger.info("Registering Kafka Consumer | Topic: {} | Kind: {} | Event: {} - {} | Handler: {} | " +
                        "Transformer: {}", event.getTopic(), event.getTechId().toString(),
                        event.getEventModel().getAddress(), event.getEventModel().getPattern(),
                        handler.getClass().getName(), handler.transformer().getClass().getName());
        });
        return builder.build();
    }

    private KafkaConsumerHandler createConsumerHandler(String sharedKey, KafkaEventMetadata metadata) {
        KafkaConsumerHandler consumerHandler = metadata.getConsumerHandler();
        if (Objects.isNull(consumerHandler)) {
            consumerHandler = KafkaConsumerHandler.createBroadcaster(vertx, sharedKey, metadata.getEventModel());
        }
        return consumerHandler.register(metadata.getTransformer(), sharedKey);
    }

}
