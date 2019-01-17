package com.nubeiot.core.kafka;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

/**
 * Register metadata to init Kafka client that lives in the end of application lifetime
 */
@Getter(AccessLevel.PACKAGE)
public final class KafkaRouter {

    private Map<String, ClientTechId> consumerTechId = new HashMap<>();
    private Map<ClientTechId, Set<KafkaEventMetadata>> consumerEvents = new HashMap<>();

    private Map<String, ClientTechId> producerTechId = new HashMap<>();
    private Set<KafkaEventMetadata> producerEvents = new HashSet<>();

    public KafkaRouter registerKafkaEvent(KafkaEventMetadata... kafkaEvents) {
        Arrays.stream(kafkaEvents).filter(Objects::nonNull).forEach(this::registerKafkaEvent);
        return this;
    }

    public KafkaRouter registerKafkaEvent(@NonNull KafkaEventMetadata kafkaEvent) {
        ClientTechId techId = kafkaEvent.getTechId();
        String topic = kafkaEvent.getTopic();
        if (kafkaEvent.getType() == KafkaClientType.CONSUMER) {
            validate(techId, topic, consumerTechId);
            this.consumerEvents.computeIfAbsent(techId, id -> new HashSet<>()).add(kafkaEvent);
        }
        if (kafkaEvent.getType() == KafkaClientType.PRODUCER) {
            validate(techId, topic, producerTechId);
            this.producerEvents.add(kafkaEvent);
        }
        return this;
    }

    private void validate(ClientTechId techId, String topic, Map<String, ClientTechId> techIdMap) {
        ClientTechId existedId = techIdMap.get(topic);
        if (Objects.nonNull(existedId) && !existedId.equals(techId)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, Strings.format(
                "Topic {0} is already registered with another pair classes: {1} - {2}", topic,
                existedId.getKeyClass().getName(), existedId.getValueClass().getName()));
        }
        techIdMap.put(topic, techId);
    }

}
