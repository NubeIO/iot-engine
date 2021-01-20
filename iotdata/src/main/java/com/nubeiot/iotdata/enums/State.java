package com.nubeiot.iotdata.enums;

import java.io.Serializable;

import io.github.zero88.qwe.dto.EnumType;
import io.github.zero88.qwe.dto.EnumType.AbstractEnumType;
import io.github.zero88.utils.Strings;

import com.fasterxml.jackson.annotation.JsonValue;

public final class State extends AbstractEnumType implements EnumType, Serializable {

    public static final State ENABLED = new State("ENABLED");
    public static final State DISABLED = new State("DISABLED");
    public static final State UNKNOWN = new State("UNKNOWN");

    private State(String type) {
        super(type);
    }

    public static State parse(String action) {
        return Strings.isBlank(action) ? UNKNOWN : EnumType.factory(action.toUpperCase(), State.class);
    }

    @JsonValue
    public String state() {
        return this.type();
    }

}
