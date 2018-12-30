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
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.StateException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateMachine {

    private final EnumMap<EventAction, StateLifeCycle> eventLifeCycles = new EnumMap<>(EventAction.class);

    private StateMachine addLifeCycle(EventAction event, StateLifeCycle lifeCycle) {
        this.eventLifeCycles.put(Objects.requireNonNull(event), Objects.requireNonNull(lifeCycle));
        return this;
    }

    private StateLifeCycle getLifeCycle(EventAction eventAction) {
        StateLifeCycle lifeCycle = eventLifeCycles.get(eventAction);
        return Objects.requireNonNull(lifeCycle, "State Machine does not support event " + eventAction);
    }

    private static StateMachine stateMachine;

    public static synchronized void init() {
        if (Objects.nonNull(stateMachine)) {
            throw new IllegalStateException("Machine is already initialized");
        }
        stateMachine = new StateMachine().addLifeCycle(EventAction.INIT, new StateLifeCycle(State.ENABLED))
                                         .addLifeCycle(EventAction.CREATE, new StateLifeCycle(State.ENABLED))
                                         .addLifeCycle(EventAction.UPDATE,
                                                       new StateLifeCycle(State.ENABLED).addFrom(State.DISABLED,
                                                                                                 State.ENABLED,
                                                                                                 State.UNAVAILABLE))
                                         .addLifeCycle(EventAction.HALT,
                                                       new StateLifeCycle(State.DISABLED).addFrom(State.ENABLED))
                                         .addLifeCycle(EventAction.REMOVE,
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

    public <T> void validate(T obj, EventAction eventAction, String objName) {
        Set<State> from = getLifeCycle(eventAction).getFrom();
        if (Objects.isNull(obj) && !from.isEmpty()) {
            throw new NotFoundException(errorExistStateMsg(objName, eventAction, false));
        } else if (Objects.nonNull(obj) && from.isEmpty()) {
            throw new AlreadyExistException(errorExistStateMsg(objName, eventAction, true));
        }
    }

    public void validateConflict(State state, EventAction eventAction, String objName) {
        Set<State> from = getLifeCycle(eventAction).getFrom();
        if (State.PENDING == state || !from.contains(state)) {
            throw new StateException(
                    String.format("%s is in state %s, cannot execute action %s", objName, state, eventAction));
        }
    }

    public State transition(EventAction eventAction, Status status) {
        if (Status.WIP == status) {
            return State.PENDING;
        }
        return getLifeCycle(eventAction).getResult();
    }

    private String errorExistStateMsg(String objName, EventAction eventAction, boolean exist) {
        return String.format("Event %s is not suitable in case of %s is %s", eventAction, objName,
                             exist ? "exist" : "non-exist");
    }

}
