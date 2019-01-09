package com.nubeiot.core.kafka;

import com.nubeiot.core.event.EventModel;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
public final class KafkaEventMetadata {

    @NonNull
    @EqualsAndHashCode.Include
    private final String topic;

    @NonNull
    @EqualsAndHashCode.Include
    private final KafkaClientType type;

    @NonNull
    private final EventModel eventModel;

}
