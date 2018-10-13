package com.nubeio.iot.share.event;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.nubeio.iot.share.enums.State;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EventType implements Serializable {
    INIT(Arrays.asList(null, State.UNAVAILABLE), State.ENABLED),
    CREATE(Arrays.asList(null, State.UNAVAILABLE), State.ENABLED),
    UPDATE(Arrays.asList(State.DISABLED, State.ENABLED), State.ENABLED),
    HALT(Collections.singletonList(State.ENABLED), State.DISABLED),
    REMOVE(Arrays.asList(State.DISABLED, State.ENABLED), State.UNAVAILABLE);

    private final List<State> onStates;
    private final State toState;

    public boolean forNew() {
        return this == INIT || this == CREATE;
    }

    public boolean forExist() {
        return !forNew();
    }
}