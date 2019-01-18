package com.nubeiot.core.kafka.handler.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;

import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.kafka.handler.KafkaHeaderConverter;
import com.nubeiot.core.kafka.handler.KafkaRecord;

/**
 * Transform {@code ConsumerRecord} to {@code EventMessage} for handling in {@code Eventbus}
 *
 * @param <K> Type of {@code ConsumerRecord} key
 * @param <V> Type of {@code ConsumerRecord} value
 * @see KafkaConsumerRecord
 * @see KafkaBroadcaster
 * @see ConsumerRecord
 * @see EventMessage
 */
public class KafkaBroadcasterTransformer<K, V> implements KafkaConsumerRecordTransformer<K, V, EventMessage> {

    public static final KafkaBroadcasterTransformer DEFAULT = new KafkaBroadcasterTransformer();

    @Override
    public EventMessage apply(KafkaConsumerRecord<K, V> record) {
        EventMessage msg = KafkaHeaderConverter.convert(record.record().headers());
        if (msg.isError()) {
            return msg;
        }
        JsonObject data = KafkaRecord.serialize(record.record()).toJson();
        return EventMessage.from(msg.getStatus(), msg.getAction(), msg.getPrevAction(), data);
    }

}
