package com.nubeiot.core.event;

import java.io.Serializable;

import lombok.Getter;

@Getter
public enum EventType implements Serializable {

    INIT, CREATE, UPDATE, HALT, REMOVE, GET_ONE, GET_LIST

}
