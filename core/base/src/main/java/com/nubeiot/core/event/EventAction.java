package com.nubeiot.core.event;

import java.io.Serializable;

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
    RETURN,
    MIGRATE,
    UNKNOWN,
    SEND,
    PUBLISH,
    MONITOR, NOTIFY,
    BATCH_CREATE,
    BATCH_UPDATE,
    BATCH_PATCH,
    BATCH_DELETE
}
