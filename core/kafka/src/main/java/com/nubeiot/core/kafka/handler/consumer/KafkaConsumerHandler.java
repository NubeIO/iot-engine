package com.nubeiot.core.kafka.handler.consumer;

import java.util.function.Consumer;
import java.util.function.Function;

import io.vertx.kafka.client.consumer.KafkaConsumerRecord;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Record handler by {@code Kafka topic} after receiving data from {@code Kafka Consumer} then transform to appropriate
 * data type
 *
 * @param <K> Type of {@code KafkaConsumerRecord} key
 * @param <V> Type of {@code KafkaConsumerRecord} value
 * @param <R> Data type after transform {@code KafkaConsumerRecord}
 * @see KafkaConsumerRecord
 * @see KafkaConsumerRecordTransformer
 * @see ConsumerDispatcher
 */
@RequiredArgsConstructor
public abstract class KafkaConsumerHandler<K, V, R, T extends KafkaConsumerRecordTransformer<K, V, R>>
    implements Consumer<KafkaConsumerRecord<K, V>> {

    @NonNull
    protected final Function<String, Object> sharedDataFunc;

    @Override
    public final void accept(KafkaConsumerRecord<K, V> record) {
        execute(transformer().apply(record));
    }

    protected abstract void execute(R result);

    @NonNull
    protected abstract T transformer();

    public abstract KafkaConsumerHandler registerTransformer(T transformer);

}
