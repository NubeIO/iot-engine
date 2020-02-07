package com.nubeiot.edge.connector.bacnet.translator;

import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder(builderClassName = "Builder")
public final class BACnetPointPropertyMetadata {

    private final ObjectType objectType;
    private final EngineeringUnits units;

}
