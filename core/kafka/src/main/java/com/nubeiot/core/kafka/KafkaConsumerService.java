package com.nubeiot.core.kafka;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumer;

import com.nubeiot.core.kafka.KafkaConfig.ConsumerCfg;
import com.nubeiot.core.kafka.handler.consumer.ConsumerDispatcher;
import com.nubeiot.core.kafka.handler.consumer.ConsumerDispatcher.Builder;
import com.nubeiot.core.kafka.handler.consumer.KafkaBroadcaster;
import com.nubeiot.core.kafka.handler.consumer.KafkaConsumerHandler;
import com.nubeiot.core.kafka.supplier.KafkaConsumerProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final Vertx vertx;
    private final ConsumerCfg config;
    private final Map<String, ClientTechId> techIdMap;
    private final Map<String, KafkaConsumer> consumers = new HashMap<>();

    static KafkaConsumerService create(Vertx vertx, ConsumerCfg config, KafkaRouter router,
                                       Function<String, Object> sharedDataFunc) {
        return new KafkaConsumerService(vertx, config, router.getConsumerTechId()).create(sharedDataFunc,
                                                                                          router.getConsumerEvents());
    }

    Collection<KafkaConsumer> consumers() {
        return Collections.unmodifiableCollection(consumers.values());
    }

    public KafkaConsumer consumer(String topic) {
        return consumers.get(topic);
    }

    private KafkaConsumerService create(Function<String, Object> sharedDataFunc,
                                        Map<ClientTechId, Set<KafkaEventMetadata>> consumerEvents) {
        Map<ClientTechId, KafkaConsumer> temp = new HashMap<>();
        techIdMap.forEach((topic, techId) -> consumers.put(topic, temp.computeIfAbsent(techId, this::create)));
        consumerEvents.forEach((clientId, metadata) -> registerHandler(temp.get(clientId), sharedDataFunc, metadata));
        return this;
    }

    private <K, V> void registerHandler(KafkaConsumer<K, V> consumer, Function<String, Object> sharedDataFunc,
                                        Set<KafkaEventMetadata> identicalTechIdEvents) {
        ConsumerDispatcher<K, V> dispatcher = createDispatcher(sharedDataFunc, identicalTechIdEvents);
        consumer.handler(dispatcher::accept).exceptionHandler(t -> logger.error("Error occurs in Kafka Consumer", t));
        consumer.subscribe(dispatcher.topics());
    }

    private <K, V> KafkaConsumer<K, V> create(ClientTechId techId) {
        return KafkaConsumerProvider.create(vertx, config, techId.getKeySerdes().deserializer(),
                                            techId.getValueSerdes().deserializer());
    }

    private <K, V> ConsumerDispatcher<K, V> createDispatcher(Function<String, Object> sharedDataFunc,
                                                             Set<KafkaEventMetadata> identicalTechIdEvents) {
        Builder<K, V> builder = ConsumerDispatcher.builder();
        identicalTechIdEvents.forEach(
            event -> builder.handler(event.getTopic(), createConsumerHandler(sharedDataFunc, event)));
        return builder.build();
    }

    private KafkaConsumerHandler createConsumerHandler(Function<String, Object> sharedFunc,
                                                       KafkaEventMetadata metadata) {
        return new KafkaBroadcaster(sharedFunc, metadata.getEventModel()).registerTransformer(
            metadata.getTransformer());
    }

}
