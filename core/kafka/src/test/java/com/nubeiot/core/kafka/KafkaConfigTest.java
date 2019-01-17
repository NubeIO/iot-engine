package com.nubeiot.core.kafka;

import java.util.Collections;

import org.apache.kafka.clients.CommonClientConfigs;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.nubeiot.core.IConfig;
import com.nubeiot.core.utils.Configs;

public class KafkaConfigTest {

    @Test
    public void test_default() throws JSONException {
        KafkaConfig from = IConfig.from(Configs.loadJsonConfig("kafka.json"), KafkaConfig.class);
        from.getConsumerConfig().put(CommonClientConfigs.CLIENT_ID_CONFIG, "consumer");
        from.getProducerConfig().put(CommonClientConfigs.CLIENT_ID_CONFIG, "producer");
        System.out.println(from.toJson().encode());

        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.getConsumerConfig().put(CommonClientConfigs.CLIENT_ID_CONFIG, "consumer");
        kafkaConfig.getProducerConfig().put(CommonClientConfigs.CLIENT_ID_CONFIG, "producer");

        JSONAssert.assertEquals(kafkaConfig.getConsumerConfig().toJson().encode(),
                                from.getConsumerConfig().toJson().encode(), JSONCompareMode.STRICT);
        JSONAssert.assertEquals(kafkaConfig.getProducerConfig().toJson().encode(),
                                from.getProducerConfig().toJson().encode(), JSONCompareMode.STRICT);
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

    @Test
    public void test_from_root() {
        KafkaConfig from = IConfig.from("{\"__app__\":{\"__kafka__\":{\"__client__\":{\"bootstrap" +
                                        ".servers\":[\"localhost:9092\"]},\"__security__\":{\"security" +
                                        ".protocol\":\"PLAINTEXT\"}}},\"__deploy__\":{\"ha\":false,\"instances\":1," +
                                        "\"maxWorkerExecuteTime\":60000000000," +
                                        "\"maxWorkerExecuteTimeUnit\":\"NANOSECONDS\",\"multiThreaded\":false," +
                                        "\"worker\":false,\"workerPoolSize\":20}}", KafkaConfig.class);
        Assert.assertEquals("PLAINTEXT", from.getProducerConfig().get("security.protocol"));
        Assert.assertEquals(Collections.singletonList("localhost:9092"),
                            from.getConsumerConfig().get("bootstrap.servers"));
    }

}
