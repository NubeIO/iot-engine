package com.nubeiot.core.kafka;

import org.apache.kafka.common.serialization.Serde;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.kafka.handler.consumer.KafkaBroadcasterTransformer;
import com.nubeiot.core.kafka.serialization.NubeKafkaSerdes;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Kafka event metadata for manage {@code KafkaConsumer} and {@code KafkaProducer} in application
 *
 * @param <K> Type of {@code Kafka Record} key
 * @param <V> Type of {@code Kafka Record} value
 * @see EventModel
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
public final class KafkaEventMetadata<K, V> {

    @NonNull
    @EqualsAndHashCode.Include
    private final String topic;
    @NonNull
    @EqualsAndHashCode.Include
    private final KafkaClientType type;
    private final EventModel eventModel;
    private final KafkaBroadcasterTransformer<K, V> transformer;
    @NonNull
    private final ClientTechId<K, V> techId;

    public static <K, V> KafkaEventMetadata<K, V> consumer(@NonNull String topic, @NonNull EventModel model,
                                                           @NonNull Class<K> keyClass, @NonNull Class<V> valueClass) {
        return consumer(topic, model, keyClass, null, valueClass, null);
    }

    public static <K, V> KafkaEventMetadata<K, V> consumer(@NonNull String topic, @NonNull EventModel model,
                                                           @NonNull Class<K> keyClass, Serde<K> keySerdes,
                                                           @NonNull Class<V> valueClass, Serde<V> valueSerdes) {
        return consumer(topic, model, null, keyClass, keySerdes, valueClass, valueSerdes);
    }

    public static <K, V> KafkaEventMetadata<K, V> consumer(@NonNull String topic, @NonNull EventModel model,
                                                           @NonNull KafkaBroadcasterTransformer<K, V> transformer,
                                                           @NonNull Class<K> keyClass, @NonNull Class<V> valueClass) {
        return consumer(topic, model, transformer, keyClass, null, valueClass, null);
    }

    /**
     * @param topic       Kafka Topic
     * @param model       Event model
     * @param transformer {@code EventMessage} transformer from {@code Kafka Consumer Record}
     * @param keyClass    {@code Kafka Consumer record} key class
     * @param keySerdes   {@code Kafka Consumer record} key serdes
     * @param valueClass  {@code Kafka Consumer record} value class
     * @param valueSerdes {@code Kafka Consumer record} value  serdes
     * @param <K>         Type of {@code Kafka Consumer record} key
     * @param <V>         Type of {@code Kafka Consumer record} value
     * @return KafkaEventMetadata represents for {@code Kafka Consumer record}
     * @see EventModel
     * @see KafkaBroadcasterTransformer
     * @see Serde
     * @see NubeKafkaSerdes
     */
    public static <K, V> KafkaEventMetadata<K, V> consumer(@NonNull String topic, @NonNull EventModel model,
                                                           KafkaBroadcasterTransformer<K, V> transformer,
                                                           @NonNull Class<K> keyClass, Serde<K> keySerdes,
                                                           @NonNull Class<V> valueClass, Serde<V> valueSerdes) {
        return new KafkaEventMetadata<>(topic, KafkaClientType.CONSUMER, model, transformer,
                                        new ClientTechId<>(keyClass, keySerdes, valueClass, valueSerdes));
    }

    public static <K, V> KafkaEventMetadata<K, V> producer(@NonNull String topic, @NonNull Class<K> keyClass,
                                                           @NonNull Class<V> valueClass) {
        return producer(topic, keyClass, null, valueClass, null);
    }

    /**
     * @param topic       Kafka Topic
     * @param keyClass    {@code Kafka Producer record} key class
     * @param keySerdes   {@code Kafka Producer record} key serdes
     * @param valueClass  {@code Kafka Producer record} value class
     * @param valueSerdes {@code Kafka Producer record} value  serdes
     * @param <K>         Type of {@code Kafka Producer record} key
     * @param <V>         Type of {@code Kafka Producer record} value
     * @return KafkaEventMetadata represents for Producer Record
     * @see Serde
     * @see NubeKafkaSerdes
     */
    public static <K, V> KafkaEventMetadata<K, V> producer(@NonNull String topic, @NonNull Class<K> keyClass,
                                                           Serde<K> keySerdes, @NonNull Class<V> valueClass,
                                                           Serde<V> valueSerdes) {
        return new KafkaEventMetadata<>(topic, KafkaClientType.PRODUCER, null, null,
                                        new ClientTechId<>(keyClass, keySerdes, valueClass, valueSerdes));
    }

}
