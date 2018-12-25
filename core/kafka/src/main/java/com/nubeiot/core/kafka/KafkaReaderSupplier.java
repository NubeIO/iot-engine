package com.nubeiot.core.kafka;

import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

import com.nubeiot.core.kafka.serialization.NubeKafkaSerdes;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaReadStream;

public interface KafkaReaderSupplier {

    /**
     * Create a new KafkaReadStream instance
     *
     * @param vertx     Vert.x instance to use
     * @param config    Kafka consumer configuration
     * @param keyType   class type for the key deserialization
     * @param valueType class type for the value deserialization
     * @return an instance of the KafkaReadStream
     */
    static <K, V> KafkaReadStream<K, V> create(Vertx vertx, Properties config, Class<K> keyType, Class<V> valueType) {
        Deserializer<K> keyDeserializer = NubeKafkaSerdes.serdeFrom(keyType).deserializer();
        Deserializer<V> valueDeserializer = NubeKafkaSerdes.serdeFrom(valueType).deserializer();
        return create(vertx, new KafkaConsumer<>(config, keyDeserializer, valueDeserializer));
    }

    /**
     * Create a new KafkaReadStream instance
     *
     * @param vertx     Vert.x instance to use
     * @param config    Kafka consumer configuration
     * @param keyType   class type for the key deserialization
     * @param valueType class type for the value deserialization
     * @return an instance of the KafkaReadStream
     */
    static <K, V> KafkaReadStream<K, V> create(Vertx vertx, Map<String, Object> config, Class<K> keyType,
                                               Class<V> valueType) {
        Deserializer<K> keyDeserializer = NubeKafkaSerdes.serdeFrom(keyType).deserializer();
        Deserializer<V> valueDeserializer = NubeKafkaSerdes.serdeFrom(valueType).deserializer();
        return create(vertx, new KafkaConsumer<>(config, keyDeserializer, valueDeserializer));
    }

    /**
     * Create a new KafkaReadStream instance
     *
     * @param vertx     Vert.x instance to use
     * @param config    Kafka consumer configuration
     * @param keyType   class type for the key deserialization
     * @param valueType class type for the value deserialization
     * @return an instance of the KafkaReadStream
     */
    static <K, V> KafkaReadStream<K, V> create(Vertx vertx, JsonObject config, Class<K> keyType, Class<V> valueType) {
        return create(vertx, config.getMap(), keyType, valueType);
    }

    /**
     * Create a new KafkaReadStream instance
     *
     * @param vertx    Vert.x instance to use
     * @param consumer native Kafka consumer instance
     * @return an instance of the KafkaReadStream
     */
    static <K, V> KafkaReadStream<K, V> create(Vertx vertx, Consumer<K, V> consumer) {
        return KafkaReadStream.create(vertx, consumer);
    }

}
