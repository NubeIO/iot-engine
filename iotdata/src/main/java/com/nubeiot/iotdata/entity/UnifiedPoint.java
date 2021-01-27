package com.nubeiot.iotdata.entity;

import java.util.UUID;

import com.nubeiot.iotdata.UnifiedIoTEntity;
import com.nubeiot.iotdata.enums.PointType;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@Accessors(fluent = true)
public class UnifiedPoint implements IPoint<UUID>, UnifiedIoTEntity {

    @NonNull
    @Default
    private final UUID key = UUID.randomUUID();
    @NonNull
    private final PointType type;
    @NonNull
    private final String deviceId;
    @Default
    private final ParticularData<IPoint> particularData = new ParticularData<>(IPoint.class);

}
