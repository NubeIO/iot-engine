package com.nubeiot.core.kafka.handler.consumer;

import java.util.function.Consumer;

import io.vertx.kafka.client.consumer.KafkaConsumerRecord;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventModel;

/**
 * Record handler by {@code Kafka topic} after receiving data from {@code Kafka Consumer} then transform to appropriate
 * data type
 *
 * @param <K> Type of {@code KafkaConsumerRecord} key
 * @param <V> Type of {@code KafkaConsumerRecord} value
 * @param <T> Type of {@code KafkaConsumerRecordTransformer}
 * @param <R> Type of transformer result
 * @see KafkaConsumerRecord
 * @see KafkaConsumerRecordTransformer
 * @see ConsumerDispatcher
 * @see SharedDataDelegate
 */
public interface KafkaConsumerHandler<K, V, T extends KafkaConsumerRecordTransformer<K, V, R>, R>
    extends Consumer<KafkaConsumerRecord<K, V>>, SharedDataDelegate {

    /**
     * Create {@code Kafka Broadcaster} that linked to a specified {@code Eventbus model}
     *
     * @param eventModel Given event model
     * @return a reference to this, so the API can be used fluently
     * @see EventModel
     */
    static KafkaConsumerHandler createBroadcaster(EventModel eventModel) {
        return new KafkaBroadcaster(eventModel);
    }

    /**
     * System will register it automatically. You don't need call it directly
     *
     * @param transformer Given transformer
     * @return a reference to this, so the API can be used fluently
     */
    KafkaConsumerHandler registerTransformer(T transformer);

    /**
     * Handler data after transforming from {@code KafkaConsumerRecord}
     *
     * @param result Result after transforming
     */
    void execute(R result);

    /**
     * @return transformer
     */
    T transformer();

    @Override
    default void accept(KafkaConsumerRecord<K, V> record) {
        execute(transformer().apply(record));
    }

}
