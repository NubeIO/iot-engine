package com.nubeiot.core.event;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;

/**
 * Keep event bus {@code address}, {@code pattern} and {@code available event types} for this {@code address}.
 *
 * @see EventAction
 * @see EventPattern
 */
@Getter
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
@Builder(builderClassName = "Builder")
@ToString(onlyExplicitlyIncluded = true)
public final class EventModel {

    @NonNull
    @EqualsAndHashCode.Include
    @ToString.Include
    private final String address;
    @lombok.Builder.Default
    @ToString.Include
    private final EventPattern pattern = EventPattern.REQUEST_RESPONSE;
    @lombok.Builder.Default
    @ToString.Include
    private final boolean local = false;
    @Singular
    @NonNull
    private final Set<EventAction> events;

    public static EventModel clone(@NonNull EventModel model, @NonNull String address) {
        return new EventModel(address, model.getPattern(), model.isLocal(), model.getEvents());
    }

    public static EventModel clone(@NonNull EventModel model, @NonNull String address, @NonNull EventPattern pattern) {
        return new EventModel(address, pattern, model.isLocal(), model.getEvents());
    }

    public Set<EventAction> getEvents() {
        return Collections.unmodifiableSet(this.events.stream().filter(Objects::nonNull).collect(Collectors.toSet()));
    }

}
