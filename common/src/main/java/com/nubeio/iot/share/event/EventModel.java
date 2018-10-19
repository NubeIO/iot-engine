package com.nubeio.iot.share.event;

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

    public static final EventModel EDGE_APP_INSTALLER = new EventModel("nubeio.edge.app.installer").add(
            EventType.CREATE, EventType.UPDATE, EventType.HALT, EventType.REMOVE, EventType.GET_ONE,
            EventType.GET_LIST);
    public static final EventModel EDGE_APP_TRANSACTION = new EventModel("nubeio.edge.app.installer.transaction").add(
            EventType.GET_ONE);

    public static final EventModel EDGE_BIOS_INSTALLER = new EventModel("nubeio.edge.bios.installer").add(
            EventType.UPDATE, EventType.GET_ONE, EventType.GET_LIST);
    public static final EventModel EDGE_BIOS_TRANSACTION = new EventModel("nubeio.edge.bios.installer.transaction").add(
            EventType.GET_ONE);
    public static final EventModel EDGE_BIOS_STATUS = new EventModel("nubeio.edge.bios.status").add(EventType.GET_ONE);

    @Getter
    @EqualsAndHashCode.Include
    private final String address;
    private final Set<EventType> events = new HashSet<>();

    EventModel add(EventType eventType) {
        this.events.add(Objects.requireNonNull(eventType));
        return this;
    }

    EventModel add(EventType... eventTypes) {
        this.events.addAll(Arrays.stream(eventTypes).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    public Set<EventType> getEvents() {
        return Collections.unmodifiableSet(this.events);
    }

}
