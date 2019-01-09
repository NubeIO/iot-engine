package com.nubeiot.core.kafka;

import java.util.Collections;

import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.client.consumer.KafkaReadStream;
import io.vertx.kafka.client.producer.KafkaWriteStream;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.kafka.supplier.KafkaReaderSupplier;
import com.nubeiot.core.kafka.supplier.KafkaWriterSupplier;
import com.nubeiot.core.utils.Configs;

@RunWith(VertxUnitRunner.class)
public class KafkaConfigTest {

    @Test
    public void test_default() throws JSONException {
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        System.out.println(from.toJson().encode());
        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.getSecurityConfig().put("security.protocol", SecurityProtocol.SSL.name);
        kafkaConfig.getSecurityConfig().put("ssl.enabled.protocols", Collections.singleton("TLSv1.2"));

        kafkaConfig.getConsumerConfig().put("enable.auto.commit", false);
        kafkaConfig.getConsumerConfig().put("auto.offset.reset", OffsetResetStrategy.EARLIEST.name().toLowerCase());
        kafkaConfig.getConsumerConfig().put("client.id", "consumer");
        kafkaConfig.getConsumerConfig().put("group.id", "nubeio");

        kafkaConfig.getProducerConfig().put("acks", "1");
        kafkaConfig.getProducerConfig().put("client.id", "producer");
        kafkaConfig.getProducerConfig().put("compression.type", "gzip");

        JSONAssert.assertEquals(kafkaConfig.getConsumerConfig().toJson().encode(),
                                from.getConsumerConfig().toJson().encode(), JSONCompareMode.STRICT);
        JSONAssert.assertEquals(kafkaConfig.getProducerConfig().toJson().encode(),
                                from.getProducerConfig().toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_can_create_consumer() {
        Vertx vertx = Vertx.vertx();
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        from.getConsumerConfig().put("bootstrap.servers", "localhost:9200");
        KafkaReadStream<String, EventMessage> reader = KafkaReaderSupplier.create(vertx, from.getConsumerConfig(),
                                                                                  String.class, EventMessage.class);
        reader.close();
    }

    @Test
    public void test_can_create_producer() {
        Vertx vertx = Vertx.vertx();
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        from.getProducerConfig().put("bootstrap.servers", "localhost:9200");
        from.getProducerConfig().put("compression.type", "none");
        KafkaWriteStream<String, EventMessage> writer = KafkaWriterSupplier.create(vertx, from.getProducerConfig(),
                                                                                   String.class, EventMessage.class);
        writer.close();
    }

}
