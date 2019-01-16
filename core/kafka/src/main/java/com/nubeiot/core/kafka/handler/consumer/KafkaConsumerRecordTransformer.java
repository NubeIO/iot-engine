package com.nubeiot.core.kafka.handler.consumer;

import java.util.function.Function;

import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;

import com.nubeiot.core.kafka.handler.KafkaRecord;

/**
 * Responsible for transform {@code KafkaConsumerRecord} to another data type that helps to handler Kafka data in
 * different service
 *
 * @param <K> Type of {@code KafkaConsumerRecord} key
 * @param <V> Type of {@code KafkaConsumerRecord} value
 * @param <R> Data type after transform {@code KafkaConsumerRecord}
 */
public interface KafkaConsumerRecordTransformer<K, V, R> extends Function<KafkaConsumerRecord<K, V>, R> {

    @Override
    R apply(KafkaConsumerRecord<K, V> consumerRecord);

    class RecordToJson<K, V> implements KafkaConsumerRecordTransformer<K, V, JsonObject> {

        @Override
        public JsonObject apply(KafkaConsumerRecord<K, V> consumerRecord) {
            return KafkaRecord.serialize(consumerRecord.record()).toJson();
        }

    }

}
