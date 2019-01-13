package com.nubeiot.core.kafka.handler;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.DateTimes;

public class KafkaRecordTest {

    private Headers headers;

    @Before
    public void setup() {
        headers = new RecordHeaders();
        headers.add(new RecordHeader("h1", "test".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void test_consumer_record() throws JSONException {
        ConsumerRecord<String, Integer> record = new ConsumerRecord<>("topic", 1, 1, "key", 1);
        KafkaRecord<ConsumerRecord<String, Integer>> cr = KafkaRecord.serialize(record);
        KafkaRecord<ConsumerRecord<String, Integer>> deserialize = KafkaRecord.toConsumer(cr.toJson());
        JSONAssert.assertEquals(cr.toJson().encode(), deserialize.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test(expected = MismatchedInputException.class)
    public void test_deserialize_consumer_record_failed() throws Throwable {
        ProducerRecord<String, Integer> record = new ProducerRecord<>("topic", 1,
                                                                      DateTimes.now().toInstant().toEpochMilli(),
                                                                      "hello", 1, headers);
        KafkaRecord<ProducerRecord<String, Integer>> pr = KafkaRecord.serialize(record);
        try {
            KafkaRecord.toConsumer(pr.toJson());
        } catch (NubeException e) {
            throw e.getCause().getCause().getCause();
        }
    }

    @Test
    public void test_producer_record() throws JSONException {
        ProducerRecord<String, Integer> record = new ProducerRecord<>("topic", 1,
                                                                      DateTimes.now().toInstant().toEpochMilli(),
                                                                      "hello", 1, headers);
        KafkaRecord<ProducerRecord<String, Integer>> pr = KafkaRecord.serialize(record);
        KafkaRecord<ProducerRecord<String, Integer>> deserialize = KafkaRecord.toProducer(pr.toJson());
        JSONAssert.assertEquals(pr.toJson().encode(), deserialize.toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test(expected = MismatchedInputException.class)
    public void test_deserialize_producer_record_failed() throws Throwable {
        ConsumerRecord<String, Integer> record = new ConsumerRecord<>("topic", 1, 1,
                                                                      DateTimes.now().toInstant().toEpochMilli(),
                                                                      TimestampType.LOG_APPEND_TIME, 1, 1, 1, "key", 1);
        KafkaRecord<ConsumerRecord<String, Integer>> pr = KafkaRecord.serialize(record);
        try {
            KafkaRecord.toProducer(pr.toJson());
        } catch (NubeException e) {
            throw e.getCause().getCause().getCause();
        }
    }

}
