package com.nubeiot.core.kafka.supplier;

import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaReadStream;
import io.vertx.kafka.client.consumer.impl.KafkaConsumerImpl;

import com.nubeiot.core.kafka.serialization.NubeKafkaSerdes;

import lombok.NonNull;

/**
 * Vertx Kafka Consumer provider
 *
 * @see NubeKafkaSerdes
 * @see io.vertx.kafka.client.consumer.KafkaConsumer
 */
public interface KafkaConsumerProvider {

    /**
     * Create a new KafkaConsumer instance
     *
     * @param <K>       type of key
     * @param <V>       type of value
     * @param vertx     Vert.x instance serialize use
     * @param config    Kafka consumer configuration
     * @param keyType   class type for the key deserialization
     * @param valueType class type for the value deserialization
     * @return an instance of the KafkaReadStream
     */
    static <K, V> io.vertx.kafka.client.consumer.KafkaConsumer<K, V> create(Vertx vertx, @NonNull JsonObject config,
                                                                            Class<K> keyType, Class<V> valueType) {
        return create(vertx, config.getMap(), keyType, valueType);
    }

    /**
     * Create a new KafkaConsumer instance
     *
     * @param <K>       type of key
     * @param <V>       type of value
     * @param vertx     Vert.x instance serialize use
     * @param config    Kafka consumer configuration
     * @param keyType   class type for the key deserialization
     * @param valueType class type for the value deserialization
     * @return an instance of the KafkaConsumer
     * @see io.vertx.kafka.client.consumer.KafkaConsumer
     */
    static <K, V> io.vertx.kafka.client.consumer.KafkaConsumer<K, V> create(Vertx vertx, Map<String, Object> config,
                                                                            @NonNull Class<K> keyType,
                                                                            @NonNull Class<V> valueType) {
        Deserializer<K> keyDeserializer = NubeKafkaSerdes.serdeFrom(keyType).deserializer();
        Deserializer<V> valueDeserializer = NubeKafkaSerdes.serdeFrom(valueType).deserializer();
        return create(vertx, config, keyDeserializer, valueDeserializer);
    }

    /**
     * Create a new KafkaConsumer instance
     *
     * @param <K>               type of key
     * @param <V>               type of value
     * @param vertx             Vert.x instance serialize use
     * @param config            Kafka consumer configuration
     * @param keyDeserializer   The key deserialization
     * @param valueDeserializer The value deserialization
     * @return an instance of the KafkaReadStream
     */
    static <K, V> io.vertx.kafka.client.consumer.KafkaConsumer<K, V> create(Vertx vertx, @NonNull JsonObject config,
                                                                            @NonNull Deserializer<K> keyDeserializer,
                                                                            @NonNull Deserializer<V> valueDeserializer) {
        return create(vertx, config.getMap(), keyDeserializer, valueDeserializer);
    }

    /**
     * Create a new KafkaConsumer instance
     *
     * @param <K>               type of key
     * @param <V>               type of value
     * @param vertx             Vert.x instance serialize use
     * @param config            Kafka consumer configuration
     * @param keyDeserializer   The key deserialization
     * @param valueDeserializer The value deserialization
     * @return an instance of the KafkaReadStream
     * @see NubeKafkaSerdes
     * @see io.vertx.kafka.client.consumer.KafkaConsumer
     */
    static <K, V> io.vertx.kafka.client.consumer.KafkaConsumer<K, V> create(@NonNull Vertx vertx,
                                                                            @NonNull Map<String, Object> config,
                                                                            @NonNull Deserializer<K> keyDeserializer,
                                                                            @NonNull Deserializer<V> valueDeserializer) {
        return createConsumer(vertx, new KafkaConsumer<>(config, keyDeserializer, valueDeserializer));
    }

    /**
     * Create a new KafkaConsumer instance
     *
     * @param vertx    Vert.x instance serialize use
     * @param consumer native Kafka consumer instance
     * @param <K>      type of key
     * @param <V>      type of value
     * @return an instance of the KafkaReadStream
     * @see Consumer
     * @see KafkaConsumer
     */
    static <K, V> io.vertx.kafka.client.consumer.KafkaConsumer<K, V> createConsumer(@NonNull Vertx vertx,
                                                                                    @NonNull Consumer<K, V> consumer) {
        return new KafkaConsumerImpl<>(KafkaReadStream.create(vertx, consumer)).registerCloseHook();
    }

}
