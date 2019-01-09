package com.nubeiot.core.kafka.handler;

import java.util.function.Function;

import org.apache.kafka.clients.producer.ProducerRecord;

import com.nubeiot.core.event.EventMessage;

/**
 * Transform from {@code EventMessage} to {@code ProducerRecord}
 *
 * @param <K> Type of {@code ProducerRecord} key
 * @param <V> Type of {@code ProducerRecord} value
 */
public class ProducerRecordTransformer<K, V> implements Function<EventMessage, ProducerRecord<K, V>> {

    @Override
    public ProducerRecord<K, V> apply(EventMessage message) {
        return new ProducerRecord<>(message.getData().getString("topic"), null);
    }

}
