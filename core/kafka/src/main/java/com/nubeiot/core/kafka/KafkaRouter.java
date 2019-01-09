package com.nubeiot.core.kafka;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.event.EventModel;

import lombok.Getter;

@Getter
public final class KafkaRouter {

    private Map<KafkaClientType, Map<String, EventModel>> kafkaEvents = new HashMap<>();

    public KafkaRouter addKafkaEvent(KafkaEventMetadata... kafkaEvents) {
        Arrays.stream(kafkaEvents).filter(Objects::nonNull).forEach(this::addKafkaEvent);
        return this;
    }

    public KafkaRouter addKafkaEvent(KafkaEventMetadata kafkaEvent) {
        kafkaEvents.computeIfAbsent(kafkaEvent.getType(), type -> new HashMap<>())
                   .putIfAbsent(kafkaEvent.getTopic(), kafkaEvent.getEventModel());
        return this;
    }

}
