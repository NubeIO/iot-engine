package com.nubeiot.iotdata.dto;

import io.github.zero88.msa.bp.dto.EnumType;
import io.github.zero88.msa.bp.dto.EnumType.AbstractEnumType;
import io.github.zero88.msa.bp.dto.PlainType;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents {@code semantic protocol} that stands for {@code communication protocol} between {@code NubeIO Edge} and
 * actual {@code device}/{@code equipment}
 */
public final class Protocol extends AbstractEnumType implements PlainType {

    public static final Protocol UNKNOWN = new Protocol("UNKNOWN");

    private Protocol(String type) { super(type); }

    public static Protocol def()  { return UNKNOWN; }

    @JsonCreator
    public static Protocol factory(String name) {
        return EnumType.factory(name, Protocol.class, def());
    }

}
