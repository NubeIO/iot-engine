package com.nubeiot.core.statemachine;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
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

    private final EnumMap<EventAction, Map<State, StateLifeCycle>> eventLifeCycles = new EnumMap<>(EventAction.class);

    public static synchronized void init() {
        if (Objects.nonNull(stateMachine)) {
            throw new IllegalStateException("Machine is already initialized");
        }
        stateMachine = new StateMachine().addLifeCycle(EventAction.INIT, new StateLifeCycle(State.ENABLED))
                                         .addLifeCycle(EventAction.CREATE, new StateLifeCycle(State.ENABLED))
                                         .addLifeCycle(EventAction.UPDATE,
                                                       new StateLifeCycle(State.ENABLED).addFrom(State.DISABLED))
                                         .addLifeCycle(EventAction.UPDATE,
                                                       new StateLifeCycle(State.DISABLED).addFrom(State.ENABLED))
                                         .addLifeCycle(EventAction.HALT,
                                                       new StateLifeCycle(State.DISABLED).addFrom(State.ENABLED))
                                         .addLifeCycle(EventAction.PATCH,
                                                       new StateLifeCycle(State.ENABLED).addFrom(State.DISABLED))
                                         .addLifeCycle(EventAction.PATCH,
                                                       new StateLifeCycle(State.DISABLED).addFrom(State.ENABLED))
                                         .addLifeCycle(EventAction.REMOVE,
                                                       new StateLifeCycle(State.UNAVAILABLE).addFrom(State.ENABLED,
                                                                                                     State.DISABLED))
                                         .addLifeCycle(EventAction.MIGRATE,
                                                       new StateLifeCycle(State.ENABLED).addFrom(State.DISABLED)
                                                                                        .addFrom(State.PENDING))
                                         .addLifeCycle(EventAction.MIGRATE,
                                                       new StateLifeCycle(State.DISABLED).addFrom(State.ENABLED));
    }

    private StateMachine addLifeCycle(EventAction event, StateLifeCycle lifeCycle) {
        Map<State, StateLifeCycle> stateStateLifeCycleMap = this.eventLifeCycles.get(Objects.requireNonNull(event));
        if (Objects.isNull(stateStateLifeCycleMap)) {
            stateStateLifeCycleMap = new EnumMap<>(State.class);
            stateStateLifeCycleMap.put(Objects.requireNonNull(lifeCycle).getResult(), lifeCycle);
            this.eventLifeCycles.put(Objects.requireNonNull(event), stateStateLifeCycleMap);
        } else {
            stateStateLifeCycleMap.put(Objects.requireNonNull(lifeCycle).getResult(), lifeCycle);
        }

        return this;
    }

    private static StateMachine stateMachine;

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
        boolean fromIsNotEmpty = eventLifeCycles.get(eventAction)
                                                .values()
                                                .stream()
                                                .anyMatch(stateLifeCycle -> Objects.nonNull(stateLifeCycle) &&
                                                                            !stateLifeCycle.getFrom().isEmpty());

        if (Objects.isNull(obj) && fromIsNotEmpty) {
            throw new NotFoundException(errorExistStateMsg(objName, eventAction, false));
        } else if (Objects.nonNull(obj) && !fromIsNotEmpty) {
            throw new AlreadyExistException(errorExistStateMsg(objName, eventAction, true));
        }
    }

    public void validateConflict(State state, EventAction eventAction, String objName, State targetState) {
        Set<State> from = new HashSet<>();
        Map<State, StateLifeCycle> stateLifeCycleMap = eventLifeCycles.get(eventAction);
        if (stateLifeCycleMap != null) {
            StateLifeCycle stateLifeCycle = stateLifeCycleMap.get(targetState);
            if (stateLifeCycle != null) {
                from = stateLifeCycle.getFrom();
            }
        }

        if (from.contains(state)) {
            return;
        }

        if ((eventAction == EventAction.MIGRATE ||eventAction == EventAction.PATCH || eventAction == EventAction.UPDATE) && targetState == state) {
            return;
        }
        throw new StateException(
            String.format("%s is in state %s, cannot execute action %s to state %s", objName, state, eventAction,
                          targetState));
    }

    public State transition(EventAction eventAction, Status status, State targetState) {
        if (Status.WIP == status) {
            return State.PENDING;
        }
        return targetState;
    }

    private String errorExistStateMsg(String objName, EventAction eventAction, boolean exist) {
        return String.format("Event %s is not suitable in case of %s is %s", eventAction, objName,
                             exist ? "exist" : "non-exist");
    }

}
