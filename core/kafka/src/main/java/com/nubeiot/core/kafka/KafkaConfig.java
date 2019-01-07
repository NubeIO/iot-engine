package com.nubeiot.core.kafka;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;

import lombok.Getter;

/**
 * Kafka config
 *
 * @see <a href="https://kafka.apache.org/documentation/#consumerconfigs">Consumer Config</a>
 * @see <a href="https://kafka.apache.org/documentation/#producerconfigs">Producer Config</a>
 */
@Getter
public final class KafkaConfig implements IConfig {

    @JsonProperty(value = ConsumerCfg.NAME)
    private ConsumerCfg consumerConfig = new ConsumerCfg();
    @JsonProperty(value = ProducerCfg.NAME)
    private ProducerCfg producerConfig = new ProducerCfg();
    @JsonProperty(value = SecurityConfig.NAME)
    private SecurityConfig securityConfig = new SecurityConfig();
    @JsonIgnore
    private boolean mergeConsumer = false;
    @JsonIgnore
    private boolean mergeProducer = false;

    @Override
    public String name() { return "__kafka__"; }

    @Override
    public Class<? extends IConfig> parent() { return NubeConfig.AppConfig.class; }

    public ConsumerCfg getConsumerConfig() {
        if (mergeConsumer) {
            return this.consumerConfig;
        }
        this.consumerConfig.putAll(this.securityConfig);
        return this.consumerConfig;
    }

    public ProducerCfg getProducerConfig() {
        if (mergeProducer) {
            return this.producerConfig;
        }
        this.producerConfig.putAll(this.securityConfig);
        return this.producerConfig;
    }

    public static class ConsumerCfg extends HashMap<String, Object> implements IConfig {

        public static final String NAME = "__consumer__";

        private static final Map<String, Object> DEFAULT;

        static {
            Serde<String> serde = Serdes.serdeFrom(String.class);
            Map<String, ?> m = new ConsumerConfig(
                ConsumerConfig.addDeserializerToConfig(new HashMap<>(), serde.deserializer(),
                                                       serde.deserializer())).values();
            m.keySet().removeIf(key -> key.matches("^(ssl|sasl|security)\\..+") || key.endsWith(".deserializer"));
            DEFAULT = Collections.unmodifiableMap(m);
        }

        ConsumerCfg() { this.putAll(DEFAULT); }

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return KafkaConfig.class; }

    }


    public static class ProducerCfg extends HashMap<String, Object> implements IConfig {

        public static final String NAME = "__producer__";
        private static final Map<String, Object> DEFAULT;

        static {
            Serde<String> serde = Serdes.serdeFrom(String.class);
            Map<String, ?> m = new ProducerConfig(
                ProducerConfig.addSerializerToConfig(new HashMap<>(), serde.serializer(), serde.serializer())).values();
            m.keySet().removeIf(key -> key.matches("^(ssl|sasl|security)\\..+") || key.endsWith(".serializer"));
            DEFAULT = Collections.unmodifiableMap(m);
        }

        ProducerCfg() { this.putAll(DEFAULT); }

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return KafkaConfig.class; }

    }


    public static class SecurityConfig extends HashMap<String, Object> implements IConfig {

        public static final String NAME = "__security__";
        private static final Map<String, Object> DEFAULT;

        static {
            Serde<String> serde = Serdes.serdeFrom(String.class);
            Map<String, ?> m = new ProducerConfig(
                ProducerConfig.addSerializerToConfig(new HashMap<>(), serde.serializer(), serde.serializer())).values();
            m.keySet().removeIf(key -> !key.matches("^(ssl|sasl|security)\\..+"));
            DEFAULT = Collections.unmodifiableMap(m);
        }

        SecurityConfig() { this.putAll(DEFAULT); }

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return KafkaConfig.class; }

    }

}
