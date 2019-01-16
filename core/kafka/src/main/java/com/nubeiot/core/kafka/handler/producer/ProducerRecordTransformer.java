package com.nubeiot.core.kafka.handler.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;

import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function3;
import io.reactivex.functions.Function4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.kafka.handler.KafkaHeaderConverter;
import com.nubeiot.core.utils.DateTimes;

import lombok.RequiredArgsConstructor;

/**
 * Transform from {@code EventMessage} to {@code ProducerRecord}
 *
 * @param <K> Type of {@code ProducerRecord} key
 * @param <V> Type of {@code ProducerRecord} value
 */
@RequiredArgsConstructor
public class ProducerRecordTransformer<K, V> implements BiFunction<String, EventMessage, ProducerRecord<K, V>>,
                                                        Function3<String, Integer, EventMessage, ProducerRecord<K, V>>,
                                                        Function4<String, Integer, K, EventMessage, ProducerRecord<K, V>> {

    private final ObjectMapper mapper;
    private final Class<V> valueClazz;
    private final String fallback;

    @Override
    public ProducerRecord<K, V> apply(String topic, EventMessage message) throws Exception {
        return apply(topic, null, null, message);
    }

    @Override
    public ProducerRecord<K, V> apply(String topic, Integer partition, EventMessage message) throws Exception {
        return apply(topic, partition, null, message);
    }

    @Override
    public ProducerRecord<K, V> apply(String topic, Integer partition, K key, EventMessage message) throws Exception {
        Headers headers = KafkaHeaderConverter.apply(message);
        long timestamp = DateTimes.now().toInstant().toEpochMilli();
        if (message.isError()) {
            return new ProducerRecord<>(topic, partition, timestamp, key, null, headers);
        }
        return new ProducerRecord<>(topic, partition, timestamp, key, (V) message.getData().getValue("data"), headers);
    }

}
