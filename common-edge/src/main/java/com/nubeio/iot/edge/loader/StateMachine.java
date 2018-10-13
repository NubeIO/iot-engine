package com.nubeio.iot.edge.loader;

import java.util.Objects;
import java.util.Optional;

import com.nubeio.iot.share.enums.State;
import com.nubeio.iot.share.enums.Status;
import com.nubeio.iot.share.event.EventType;
import com.nubeio.iot.share.exceptions.StateException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateMachine {

    public static <T> void validate(T obj, EventType eventType, String objName) {
        if (Objects.isNull(obj) && eventType.forExist()) {
            throw new StateException(errorExistStateMsg(objName, eventType, false));
        } else if (Objects.nonNull(obj) && eventType.forNew()) {
            throw new StateException(errorExistStateMsg(objName, eventType, true));
        }
    }

    public static <T> void validate(Optional<T> obj, EventType eventType, String objName) {
        if (!obj.isPresent() && eventType.forExist()) {
            throw new StateException(errorExistStateMsg(objName, eventType, false));
        } else if (obj.isPresent() && eventType.forNew()) {
            throw new StateException(errorExistStateMsg(objName, eventType, true));
        }
    }

    public static void validateConflict(State state, EventType eventType, String objName) {
        if (State.PENDING == state || !eventType.getOnStates().contains(state)) {
            throw new StateException(
                    String.format("%s is in state %s, cannot execute action %s", objName, state, eventType));
        }
    }

    public static State transition(EventType eventType, Status status) {
        if (Status.WIP == status) {
            return State.PENDING;
        }
        return eventType.getToState();
    }

    private static String errorExistStateMsg(String objName, EventType eventType, boolean exist) {
        return String.format("Event %s is not suitable in case of %s is %s", eventType, objName,
                             exist ? "exist" : "non-exist");
    }

}
