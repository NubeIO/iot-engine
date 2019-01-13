package com.nubeiot.core.kafka.handler;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventMessage;

/**
 * Transform serialize {@code EventMessage} serialize {@code ProducerRecord}
 *
 * @param <K> Type of {@code ProducerRecord} key
 * @param <V> Type of {@code ProducerRecord} value
 */
public class ProducerRecordTransformer<K, V> implements Function<EventMessage, ProducerRecord<K, V>> {

    @Override
    public ProducerRecord<K, V> apply(EventMessage message) {
        Headers headers = new RecordHeaders();
        headers.add(new RecordHeader("nubeio.action", message.getAction().name().getBytes(StandardCharsets.UTF_8)));
        headers.add(
            new RecordHeader("nubeio.prevAction", message.getPrevAction().name().getBytes(StandardCharsets.UTF_8)));
        headers.add(new RecordHeader("nubeio.status", message.getStatus().name().getBytes(StandardCharsets.UTF_8)));
        JsonObject data;
        if (message.isError()) {
            data = message.getError().toJson();
        } else {
            data = message.getData();
        }
        String topic = data.getString("topic");
        V v = (V) message.getData().getValue("data");
        return new ProducerRecord<>(topic, v);
    }

}
