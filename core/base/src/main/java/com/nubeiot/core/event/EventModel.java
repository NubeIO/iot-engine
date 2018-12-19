package com.nubeiot.core.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public final class EventModel {

    @Getter
    @EqualsAndHashCode.Include
    private final String address;
    private final Set<EventType> events = new HashSet<>();

    public EventModel add(EventType eventType) {
        this.events.add(Objects.requireNonNull(eventType));
        return this;
    }

    public EventModel add(EventType... eventTypes) {
        this.events.addAll(Arrays.stream(eventTypes).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    public Set<EventType> getEvents() {
        return Collections.unmodifiableSet(this.events);
    }

}
