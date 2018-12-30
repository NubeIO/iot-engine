package com.nubeiot.buildscript.jooq;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@Builder(builderClassName = "Builder")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public final class CustomDataType {

    @EqualsAndHashCode.Include
    @NonNull
    private final String className;
    @NonNull
    private final String parser;
    @NonNull
    private final String converter;

}
