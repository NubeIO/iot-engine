package com.nubeiot.core.kafka.handler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.Test;

import io.vertx.core.json.JsonObject;

public class ConsumerRecordTransformerTest {

    @Test
    public void test() {
        Headers headers = new RecordHeaders();
        headers.add(new RecordHeader("h1", "test".getBytes(StandardCharsets.UTF_8)));
        ConsumerRecord<String, Integer> a = new ConsumerRecord<>("topic", 1, 1, ConsumerRecord.NO_TIMESTAMP,
                                                                 TimestampType.NO_TIMESTAMP_TYPE,
                                                                 (long) ConsumerRecord.NULL_CHECKSUM,
                                                                 ConsumerRecord.NULL_SIZE, ConsumerRecord.NULL_SIZE,
                                                                 "key", 1, headers);
        System.out.println(new JsonObject(RecordMixin.DEFAULT.convertValue(a, Map.class)).encodePrettily());
    }

}
