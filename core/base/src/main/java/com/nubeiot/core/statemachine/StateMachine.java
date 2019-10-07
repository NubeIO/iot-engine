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

    private static StateMachine stateMachine;
    private final EnumMap<EventAction, Map<State, StateLifeCycle>> eventLifeCycles = new EnumMap<>(EventAction.class);

    public static synchronized void init() {
        if (Objects.nonNull(stateMachine)) {
            throw new IllegalStateException("Machine is already initialized");
        }
        stateMachine = new StateMachine().addLifeCycle(EventAction.INIT,
                                                       new StateLifeCycle(State.ENABLED).addFrom(State.NONE, null))
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

    public static StateMachine instance() {
        return stateMachine;
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

    public <T> void validate(T obj, EventAction action, String objName) {
        StateLifeCycle lifeCycle = eventLifeCycles.get(action).values().stream().findAny().orElse(null);
        if (lifeCycle == null) {
            throw new IllegalArgumentException("Unsupported action " + action);
        }
        if (lifeCycle.acceptBothNullAndNonNull()) {
            return;
        }
        if (Objects.isNull(obj) && !lifeCycle.isAcceptNonExisted()) {
            throw new NotFoundException(errorStateMsg(objName, action, false));
        } else if (Objects.nonNull(obj) && lifeCycle.isAcceptNonExisted()) {
            throw new AlreadyExistException(errorStateMsg(objName, action, true));
        }
    }

    public void validateConflict(State state, EventAction action, String objName, State targetState) {
        Set<State> from = new HashSet<>();
        Map<State, StateLifeCycle> stateLifeCycleMap = eventLifeCycles.get(action);
        if (stateLifeCycleMap != null) {
            StateLifeCycle stateLifeCycle = stateLifeCycleMap.get(targetState);
            if (stateLifeCycle != null) {
                from = stateLifeCycle.getFrom();
            }
        }

        if (from.contains(state)) {
            return;
        }

        if ((action == EventAction.MIGRATE || action == EventAction.PATCH || action == EventAction.UPDATE) &&
            targetState == state) {
            return;
        }
        throw new StateException(
            String.format("%s is in state %s, cannot execute action %s to state %s", objName, state, action,
                          targetState));
    }

    public State transition(EventAction action, Status status, State targetState) {
        if (Status.WIP == status) {
            return State.PENDING;
        }
        return targetState;
    }

    private String errorStateMsg(String objName, EventAction action, boolean exist) {
        return String.format("Event %s is not suitable in case of %s is %s", action, objName,
                             exist ? "exist" : "non-exist");
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
            this.from.add(state);
            return this;
        }

        StateLifeCycle addFrom(State... states) {
            this.from.addAll(Arrays.stream(states).collect(Collectors.toList()));
            return this;
        }

        Set<State> getFrom() {
            return Collections.unmodifiableSet(from);
        }

        boolean isAcceptNonExisted() {
            return from.isEmpty() || from.stream().allMatch(state -> Objects.isNull(state) || state == State.NONE);
        }

        boolean acceptBothNullAndNonNull() {
            return !from.isEmpty() && from.stream().allMatch(state -> Objects.isNull(state) || state == State.NONE);
        }

    }

}
