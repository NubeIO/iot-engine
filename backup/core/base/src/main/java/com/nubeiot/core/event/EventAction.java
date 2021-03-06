package com.nubeiot.core.event;

import java.io.Serializable;

import io.github.zero88.utils.Functions;
import io.github.zero88.utils.Strings;

import lombok.Getter;

/**
 * Defines {@code action} for {@code Eventbus}
 */
@Getter
public enum EventAction implements Serializable {

    INIT,
    CREATE,
    UPDATE,
    PATCH,
    HALT,
    REMOVE,
    GET_ONE,
    GET_LIST,
    CREATE_OR_UPDATE,
    RETURN,
    MIGRATE,
    UNKNOWN,
    SEND,
    PUBLISH,
    MONITOR,
    DISCOVER,
    NOTIFY,
    NOTIFY_ERROR,
    SYNC,
    BATCH_CREATE,
    BATCH_UPDATE,
    BATCH_PATCH,
    BATCH_DELETE;

    public static EventAction parse(String action) {
        return Strings.isBlank(action)
               ? UNKNOWN
               : Functions.getOrDefault(UNKNOWN, () -> EventAction.valueOf(action.toUpperCase()));
    }
}
