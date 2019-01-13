package com.nubeiot.core.kafka.handler;

import java.util.function.Function;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventMessage;

/**
 * Transform {@code ConsumerRecord} serialize {@code EventMessage} for handling in {@code Eventbus}
 *
 * @param <K> Type of {@code ConsumerRecord} key
 * @param <V> Type of {@code ConsumerRecord} value
 * @see ConsumerRecord
 * @see EventMessage
 */
public class ConsumerRecordTransformer<K, V> implements Function<ConsumerRecord<K, V>, EventMessage> {

    @Override
    public EventMessage apply(ConsumerRecord<K, V> record) {
        Headers headers = record.headers();

        headers.headers("status");
        //        if (value instanceof EventMessage) {
        //            EventMessage msg = (EventMessage) value;
        //            return msg;
        //        }
        return EventMessage.success(EventAction.UNKNOWN, KafkaRecord.serialize(record).toJson());
    }

}
