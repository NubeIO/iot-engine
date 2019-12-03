package com.nubeiot.core.event;

import java.io.Serializable;

import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Strings;

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
