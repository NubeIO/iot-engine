package com.nubeiot.core.statemachine;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.StateException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateMachine {

    private final EnumMap<EventType, StateLifeCycle> eventLifeCycles = new EnumMap<>(EventType.class);

    private StateMachine addLifeCycle(EventType event, StateLifeCycle lifeCycle) {
        this.eventLifeCycles.put(Objects.requireNonNull(event), Objects.requireNonNull(lifeCycle));
        return this;
    }

    private StateLifeCycle getLifeCycle(EventType eventType) {
        StateLifeCycle lifeCycle = eventLifeCycles.get(eventType);
        return Objects.requireNonNull(lifeCycle, "State Machine does not support event " + eventType);
    }

    private static StateMachine stateMachine;

    public static synchronized void init() {
        if (Objects.nonNull(stateMachine)) {
            throw new IllegalStateException("Machine is already initialized");
        }
        stateMachine = new StateMachine().addLifeCycle(EventType.INIT, new StateLifeCycle(State.ENABLED))
                                         .addLifeCycle(EventType.CREATE, new StateLifeCycle(State.ENABLED))
                                         .addLifeCycle(EventType.UPDATE,
                                                       new StateLifeCycle(State.ENABLED).addFrom(State.DISABLED,
                                                                                                 State.ENABLED,
                                                                                                 State.UNAVAILABLE))
                                         .addLifeCycle(EventType.HALT,
                                                       new StateLifeCycle(State.DISABLED).addFrom(State.ENABLED))
                                         .addLifeCycle(EventType.REMOVE,
                                                       new StateLifeCycle(State.UNAVAILABLE).addFrom(State.ENABLED,
                                                                                                     State.DISABLED));
    }

    public static StateMachine instance() {
        return stateMachine;
    }

    @RequiredArgsConstructor
    private static class StateLifeCycle {

        private final Set<State> from = new HashSet<>();
        @Getter
        private final State transition;
        @Getter
        private final State result;

        StateLifeCycle(State result) {
            this(State.PENDING, result);
        }

        StateLifeCycle addFrom(State state) {
            this.from.add(Objects.requireNonNull(state));
            return this;
        }

        StateLifeCycle addFrom(State... states) {
            this.from.addAll(Arrays.stream(states).filter(Objects::nonNull).collect(Collectors.toList()));
            return this;
        }

        public Set<State> getFrom() {
            return Collections.unmodifiableSet(from);
        }

    }

    public <T> void validate(T obj, EventType eventType, String objName) {
        Set<State> from = getLifeCycle(eventType).getFrom();
        if (Objects.isNull(obj) && !from.isEmpty()) {
            throw new NotFoundException(errorExistStateMsg(objName, eventType, false));
        } else if (Objects.nonNull(obj) && from.isEmpty()) {
            throw new AlreadyExistException(errorExistStateMsg(objName, eventType, true));
        }
    }

    public void validateConflict(State state, EventType eventType, String objName) {
        Set<State> from = getLifeCycle(eventType).getFrom();
        if (State.PENDING == state || !from.contains(state)) {
            throw new StateException(
                    String.format("%s is in state %s, cannot execute action %s", objName, state, eventType));
        }
    }

    public State transition(EventType eventType, Status status) {
        if (Status.WIP == status) {
            return State.PENDING;
        }
        return getLifeCycle(eventType).getResult();
    }

    private String errorExistStateMsg(String objName, EventType eventType, boolean exist) {
        return String.format("Event %s is not suitable in case of %s is %s", eventType, objName,
                             exist ? "exist" : "non-exist");
    }

}
