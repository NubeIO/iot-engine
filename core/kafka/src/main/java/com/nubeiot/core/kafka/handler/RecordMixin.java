package com.nubeiot.core.kafka.handler;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.record.TimestampType;

import io.vertx.core.json.Json;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.nubeiot.core.dto.JsonData;

public interface RecordMixin<T, K, V> extends JsonData {

    ObjectMapper DEFAULT = Json.mapper.copy()
                                      .addMixIn(ConsumerRecord.class, ConsumerRecordMixin.class)
                                      .addMixIn(ProducerRecord.class, ProducerRecordMixin.class)
                                      .addMixIn(ByteBuffer.class, ByteBufferIgnoreMixin.class)
                                      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
                                      .setVisibility(PropertyAccessor.SETTER, Visibility.NONE)
                                      .setVisibility(PropertyAccessor.GETTER, Visibility.NONE)
                                      .setVisibility(PropertyAccessor.CREATOR, Visibility.NONE)
                                      .registerModule(new Jdk8Module());

    @Override
    default ObjectMapper mapper() {
        return DEFAULT;
    }

    @JsonIgnoreType
    abstract class ByteBufferIgnoreMixin {}


    abstract class ConsumerRecordMixin<K, V> implements RecordMixin<ConsumerRecord, K, V> {

        @JsonCreator
        public ConsumerRecordMixin(@JsonProperty String topic, @JsonProperty int partition, @JsonProperty long offset,
                                   @JsonProperty long timestamp, @JsonProperty TimestampType timestampType,
                                   @JsonProperty Long checksum, @JsonProperty int serializedKeySize,
                                   @JsonProperty int serializedValueSize, @JsonProperty K key, @JsonProperty V value,
                                   @JsonProperty Headers headers, @JsonProperty Optional<Integer> leaderEpoch) {
        }

    }


    abstract class ProducerRecordMixin<K, V> implements RecordMixin<ProducerRecord, K, V> {

        public ProducerRecordMixin(@JsonProperty String topic, @JsonProperty Integer partition,
                                   @JsonProperty Headers headers, @JsonProperty K key, @JsonProperty V value,
                                   @JsonProperty Long timestamp) {}

    }

}
