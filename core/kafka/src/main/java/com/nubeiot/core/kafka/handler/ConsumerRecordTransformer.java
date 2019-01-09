package com.nubeiot.core.kafka.handler;

import java.util.function.Function;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;

/**
 * Transform {@code ConsumerRecord} to {@code EventMessage} for handling in {@code Eventbus}
 *
 * @param <K> Type of {@code ConsumerRecord} key
 * @param <V> Type of {@code ConsumerRecord} value
 * @see ConsumerRecord
 * @see EventMessage
 */
public class ConsumerRecordTransformer<K, V> implements Function<ConsumerRecord<K, V>, EventMessage> {

    @Override
    public EventMessage apply(ConsumerRecord<K, V> record) {
        V value = record.value();
        if (value instanceof EventMessage) {
            return (EventMessage) value;
        }
        return EventMessage.success(EventAction.UNKNOWN, record);
    }

}
