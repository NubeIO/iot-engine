package com.nubeiot.iotdata.dto;

import io.github.zero88.msa.bp.dto.EnumType;
import io.github.zero88.msa.bp.dto.EnumType.AbstractEnumType;
import io.github.zero88.msa.bp.dto.PlainType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.iotdata.dto.IoTNotion.IoTChunkNotion;

public final class TransducerType extends AbstractEnumType implements PlainType, IoTChunkNotion {

    public static final TransducerType SENSOR = new TransducerType("SENSOR");
    public static final TransducerType ACTUATOR = new TransducerType("ACTUATOR");

    private TransducerType(String type) {
        super(type);
    }

    public static TransducerType def() { return SENSOR; }

    @JsonCreator
    public static TransducerType factory(String name) {
        return EnumType.factory(name, TransducerType.class, def());
    }

}
