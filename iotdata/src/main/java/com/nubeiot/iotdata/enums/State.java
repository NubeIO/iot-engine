package com.nubeiot.iotdata.enums;

import io.github.zero88.qwe.dto.EnumType;
import io.github.zero88.qwe.dto.EnumType.AbstractEnumType;
import io.github.zero88.utils.Strings;

import com.fasterxml.jackson.annotation.JsonValue;
import com.nubeiot.iotdata.IoTEnum;

public final class State extends AbstractEnumType implements IoTEnum {

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
