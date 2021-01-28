package com.nubeiot.iotdata.entity;

import java.util.UUID;

import com.nubeiot.iotdata.UnifiedIoTEntity;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@SuperBuilder
@Accessors(fluent = true)
public class UnifiedPoint extends AbstractPoint<UUID> implements UnifiedIoTEntity {

    @Default
    private final ParticularData<IPoint> particularData = new ParticularData<>(IPoint.class);

}
