package com.nubeiot.core.kafka;

import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serializer;

import com.nubeiot.core.kafka.serialization.NubeKafkaSerdes;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaWriteStream;

/**
 * Kafka Write Stream supplier
 *
 * @see NubeKafkaSerdes
 * @see KafkaWriteStream
 */
public interface KafkaWriterSupplier {

    /**
     * Create a new KafkaWriteStream instance
     *
     * @param vertx     Vert.x instance to use
     * @param config    Kafka producer configuration
     * @param keyType   class type for the key serialization
     * @param valueType class type for the value serialization
     * @param <K>       type of key
     * @param <V>       type of value
     * @return an instance of the KafkaWriteStream
     */
    static <K, V> KafkaWriteStream<K, V> create(Vertx vertx, Properties config, Class<K> keyType, Class<V> valueType) {
        Serializer<K> keySerializer = NubeKafkaSerdes.serdeFrom(keyType).serializer();
        Serializer<V> valueSerializer = NubeKafkaSerdes.serdeFrom(valueType).serializer();
        return create(vertx, new KafkaProducer<>(config, keySerializer, valueSerializer));
    }

    /**
     * Create a new KafkaWriteStream instance
     *
     * @param vertx     Vert.x instance to use
     * @param config    Kafka producer configuration
     * @param keyType   class type for the key serialization
     * @param valueType class type for the value serialization
     * @param <K>       type of key
     * @param <V>       type of value
     * @return an instance of the KafkaWriteStream
     */
    static <K, V> KafkaWriteStream<K, V> create(Vertx vertx, Map<String, Object> config, Class<K> keyType,
                                                Class<V> valueType) {
        Serializer<K> keySerializer = NubeKafkaSerdes.serdeFrom(keyType).serializer();
        Serializer<V> valueSerializer = NubeKafkaSerdes.serdeFrom(valueType).serializer();
        return create(vertx, new KafkaProducer<>(config, keySerializer, valueSerializer));
    }

    /**
     * Create a new KafkaWriteStream instance
     *
     * @param vertx     Vert.x instance to use
     * @param config    Kafka producer configuration
     * @param keyType   class type for the key serialization
     * @param valueType class type for the value serialization
     * @param <K>       type of key
     * @param <V>       type of value
     * @return an instance of the KafkaWriteStream
     */
    static <K, V> KafkaWriteStream<K, V> create(Vertx vertx, JsonObject config, Class<K> keyType, Class<V> valueType) {
        return create(vertx, config.getMap(), keyType, valueType);
    }

    /**
     * Create a new KafkaWriteStream instance
     *
     * @param vertx    Vert.x instance to use
     * @param producer native Kafka producer instance
     * @param <K>      type of key
     * @param <V>      type of value
     * @return an instance of the KafkaWriteStream
     * @see Producer
     * @see KafkaProducer
     */
    static <K, V> KafkaWriteStream<K, V> create(Vertx vertx, Producer<K, V> producer) {
        return KafkaWriteStream.create(vertx, producer);
    }

}
