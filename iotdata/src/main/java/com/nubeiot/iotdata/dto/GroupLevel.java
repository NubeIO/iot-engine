package com.nubeiot.iotdata.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.zero88.msa.bp.dto.EnumType;
import io.github.zero88.msa.bp.dto.EnumType.AbstractEnumType;
import io.github.zero88.msa.bp.dto.PlainType;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.NonNull;

public final class GroupLevel extends AbstractEnumType implements PlainType, IoTNotion {

    public static final GroupLevel EDGE = new GroupLevel("EDGE", null);
    public static final GroupLevel NETWORK = new GroupLevel("NETWORK", Collections.singletonList(EDGE));
    public static final GroupLevel DEVICE = new GroupLevel("DEVICE", Collections.singletonList(NETWORK));
    public static final GroupLevel FOLDER = new GroupLevel("FOLDER", Arrays.asList(EDGE, NETWORK, DEVICE));

    @Getter
    private final Set<GroupLevel> possibleParents;

    private GroupLevel(@NonNull String type, List<GroupLevel> possibleParents) {
        super(type);
        this.possibleParents = Objects.isNull(possibleParents)
                               ? Collections.emptySet()
                               : new HashSet<>(possibleParents);
    }

    @JsonCreator
    public static GroupLevel factory(String type) {
        return EnumType.factory(type, GroupLevel.class);
    }

    public boolean isRoot() {
        return possibleParents.isEmpty();
    }

}
