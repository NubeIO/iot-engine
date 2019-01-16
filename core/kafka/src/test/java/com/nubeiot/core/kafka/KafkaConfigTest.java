package com.nubeiot.core.kafka;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.kafka.supplier.KafkaConsumerProvider;
import com.nubeiot.core.kafka.supplier.KafkaProducerSupplier;
import com.nubeiot.core.utils.Configs;

@RunWith(VertxUnitRunner.class)
public class KafkaConfigTest {

    @Test
    public void test_default() throws JSONException {
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        System.out.println(from.toJson().encode());

        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.getConsumerConfig().put("client.id", "consumer");
        kafkaConfig.getProducerConfig().put("client.id", "producer");

        JSONAssert.assertEquals(kafkaConfig.getConsumerConfig().toJson().encode(),
                                from.getConsumerConfig().toJson().encode(), JSONCompareMode.STRICT);
        JSONAssert.assertEquals(kafkaConfig.getProducerConfig().toJson().encode(),
                                from.getProducerConfig().toJson().encode(), JSONCompareMode.STRICT);
    }

    @Test
    public void test_can_create_consumer() {
        Vertx vertx = Vertx.vertx();
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        from.getConsumerConfig().put("bootstrap.servers", "localhost:9092");
        KafkaConsumer<String, EventMessage> consumer = KafkaConsumerProvider.create(vertx, from.getConsumerConfig(),
                                                                                    String.class, EventMessage.class);
        consumer.close();
    }

    @Test
    public void test_can_create_producer() {
        Vertx vertx = Vertx.vertx();
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        from.getProducerConfig().put("bootstrap.servers", "localhost:9092");
        from.getProducerConfig().put("compression.type", "none");
        KafkaProducer<String, EventMessage> producer = KafkaProducerSupplier.create(vertx, from.getProducerConfig(),
                                                                                    String.class, EventMessage.class);
        producer.close();
    }

    @Test
    public void test_topic() {
        KafkaConfig cfg = new KafkaConfig();
        System.out.println(cfg.getTopicConfig().toJson());
        System.out.println(cfg.getSecurityConfig().toJson());
        System.out.println(cfg.getClientConfig().toJson());
        System.out.println(cfg.getProducerConfig().toJson());
        System.out.println(cfg.getConsumerConfig().toJson());
    }

}
