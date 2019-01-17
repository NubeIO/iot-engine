package com.nubeiot.core.kafka;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.Reflections.ReflectionField;

import lombok.Getter;

/**
 * Kafka config
 *
 * @see <a href="https://kafka.apache.org/documentation/#adminclientconfigs">Admin client Config</a>
 * @see <a href="https://kafka.apache.org/documentation/#topicconfigs">Topic Config</a>
 * @see <a href="https://kafka.apache.org/documentation/#consumerconfigs">Consumer Config</a>
 * @see <a href="https://kafka.apache.org/documentation/#producerconfigs">Producer Config</a>
 */
public final class KafkaConfig implements IConfig {

    @JsonProperty(value = ClientCfg.NAME)
    private ClientCfg clientConfig = new ClientCfg();
    @Getter
    @JsonProperty(value = TopicCfg.NAME)
    private TopicCfg topicConfig = new TopicCfg();
    @JsonProperty(value = ConsumerCfg.NAME)
    private ConsumerCfg consumerConfig = new ConsumerCfg();
    @JsonProperty(value = ProducerCfg.NAME)
    private ProducerCfg producerConfig = new ProducerCfg();

    /**
     * Security part that applied among but can be overridden in {@link ConsumerCfg}, {@link ProducerCfg} and {@link
     * ClientCfg}
     */
    @Getter
    @JsonProperty(value = SecurityConfig.NAME)
    private SecurityConfig securityConfig = new SecurityConfig();

    @JsonIgnore
    private AtomicBoolean hasMergedAdmin = new AtomicBoolean(false);
    @JsonIgnore
    private AtomicBoolean hasMergedConsumer = new AtomicBoolean(false);
    @JsonIgnore
    private AtomicBoolean hasMergedProducer = new AtomicBoolean(false);

    @Override
    public String name() { return "__kafka__"; }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

    public ConsumerCfg getConsumerConfig() {
        if (hasMergedConsumer.get()) {
            return this.consumerConfig;
        }
        return computeCfg(hasMergedConsumer, consumerConfig, true);
    }

    public ProducerCfg getProducerConfig() {
        if (hasMergedProducer.get()) {
            return this.producerConfig;
        }
        return computeCfg(hasMergedProducer, producerConfig, true);
    }

    public ClientCfg getClientConfig() {
        if (hasMergedAdmin.get()) {
            return this.clientConfig;
        }
        return computeCfg(hasMergedAdmin, clientConfig, false);
    }

    private synchronized <T extends Map<String, Object>> T computeCfg(AtomicBoolean flag, T config,
                                                                      boolean includeClient) {
        Map<String, Object> cfg = new HashMap<>(this.securityConfig);
        if (includeClient) {
            cfg.putAll(this.clientConfig);
        }
        cfg.entrySet()
           .stream()
           .filter(entry -> Objects.nonNull(entry.getValue()))
           .forEach(entry -> config.merge(entry.getKey(), entry.getValue(), (v1, v2) -> v1));
        flag.set(true);
        return config;
    }

    public static class ClientCfg extends HashMap<String, Object> implements IConfig {

        public static final String NAME = "__client__";

        private static final Map<String, Object> DEFAULT;

        static {
            Map<String, ?> m = new AdminClientConfig(
                Collections.singletonMap("bootstrap.servers", "localhost:9092")).values();
            m.keySet().removeIf(key -> key.matches("^(ssl|sasl|security)\\..+"));
            DEFAULT = Collections.unmodifiableMap(m);
        }

        ClientCfg() {
            this.putAll(DEFAULT);
        }

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return KafkaConfig.class; }

    }


    public static class TopicCfg extends HashMap<String, Object> implements IConfig {

        public static final String NAME = "__topic__";
        private static final Set<String> KEYS = new HashSet<>(
            ReflectionField.getConstants(TopicConfig.class, String.class, f -> f.getName().endsWith("_CONFIG")));

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return KafkaConfig.class; }

        TopicCfg() {
            KEYS.forEach(c -> this.put(c, null));
            this.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE);
            this.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "producer");
            this.put(TopicConfig.MESSAGE_FORMAT_VERSION_CONFIG, "2.1-IV2");
            this.put(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG, "CreateTime");
            this.put(TopicConfig.MESSAGE_TIMESTAMP_DIFFERENCE_MAX_MS_CONFIG, 9223372036854775807L);
            this.put(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, 1000012);
            this.put(TopicConfig.MESSAGE_DOWNCONVERSION_ENABLE_CONFIG, false);
            this.put(TopicConfig.DELETE_RETENTION_MS_CONFIG, 86400000L);
            this.put(TopicConfig.FILE_DELETE_DELAY_MS_CONFIG, 60000);
            this.put(TopicConfig.FLUSH_MESSAGES_INTERVAL_CONFIG, 9223372036854775807L);
            this.put(TopicConfig.FLUSH_MS_CONFIG, 9223372036854775807L);
            this.put(TopicConfig.INDEX_INTERVAL_BYTES_CONFIG, 4096);
            this.put(TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG, 0.5);
            this.put(TopicConfig.MIN_COMPACTION_LAG_MS_CONFIG, 0);
            this.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, 1);
            this.put(TopicConfig.PREALLOCATE_CONFIG, false);
            this.put(TopicConfig.RETENTION_BYTES_CONFIG, -1);
            this.put(TopicConfig.RETENTION_MS_CONFIG, 604800000);
            this.put(TopicConfig.SEGMENT_BYTES_CONFIG, 1073741824);
            this.put(TopicConfig.SEGMENT_INDEX_BYTES_CONFIG, 10485760);
            this.put(TopicConfig.SEGMENT_JITTER_MS_CONFIG, 0);
            this.put(TopicConfig.SEGMENT_MS_CONFIG, 604800000);
            this.put(TopicConfig.UNCLEAN_LEADER_ELECTION_ENABLE_CONFIG, false);
        }

        @Override
        public JsonObject toJson() {
            List<String> unsupported = this.keySet()
                                           .parallelStream()
                                           .filter(k -> !KEYS.contains(k))
                                           .collect(Collectors.toList());
            if (unsupported.isEmpty()) {
                return IConfig.super.toJson();
            }
            throw new NubeException(ErrorCode.INVALID_ARGUMENT,
                                    "Kafka Topic configuration unsupported these keys: " + unsupported);
        }

    }


    public static class ConsumerCfg extends HashMap<String, Object> implements IConfig {

        public static final String NAME = "__consumer__";

        private static final Map<String, Object> DEFAULT;

        static {
            Serde<String> serde = Serdes.serdeFrom(String.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) new ConsumerConfig(
                ConsumerConfig.addDeserializerToConfig(new HashMap<>(), serde.deserializer(),
                                                       serde.deserializer())).values();
            m.keySet()
             .removeIf(key -> key.matches("^(ssl|sasl|security)\\..+") || key.endsWith(".deserializer") ||
                              ClientCfg.DEFAULT.containsKey(key));
            m.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
            m.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name().toLowerCase());
            m.put(ConsumerConfig.GROUP_ID_CONFIG, "NubeIO");
            m.put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer-" + UUID.randomUUID().toString());
            m.computeIfPresent(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                               (s, o) -> convertListClass((List) o));
            DEFAULT = Collections.unmodifiableMap(m);
        }

        private static Object convertListClass(List o) {
            return o.stream().map(v -> ((Class) v).getName()).collect(Collectors.toList());
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
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) new ProducerConfig(
                ProducerConfig.addSerializerToConfig(new HashMap<>(), serde.serializer(), serde.serializer())).values();
            m.keySet()
             .removeIf(key -> key.matches("^(ssl|sasl|security)\\..+") || key.endsWith(".serializer") ||
                              ClientCfg.DEFAULT.containsKey(key));
            if (Objects.isNull(m.get(ProducerConfig.TRANSACTIONAL_ID_CONFIG))) {
                m.remove(ProducerConfig.TRANSACTIONAL_ID_CONFIG);
            }
            m.put(ProducerConfig.ACKS_CONFIG, "1");
            m.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
            m.put(ProducerConfig.CLIENT_ID_CONFIG, "producer-" + UUID.randomUUID().toString());
            m.computeIfPresent(ProducerConfig.PARTITIONER_CLASS_CONFIG, (s, o) -> ((Class) o).getName());
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
            ConfigDef configDef = new ConfigDef().withClientSaslSupport().withClientSslSupport();
            Map<String, Object> m = configDef.defaultValues();
            m.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            m.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, Collections.singletonList("TLSv1.2"));
            DEFAULT = Collections.unmodifiableMap(m);
        }

        SecurityConfig() { this.putAll(DEFAULT); }

        @Override
        public String name() { return NAME; }

        @Override
        public Class<? extends IConfig> parent() { return KafkaConfig.class; }

    }

}
