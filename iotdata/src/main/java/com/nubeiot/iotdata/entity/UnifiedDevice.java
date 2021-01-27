package com.nubeiot.iotdata.entity;

import java.util.UUID;

import com.nubeiot.iotdata.UnifiedIoTEntity;
import com.nubeiot.iotdata.enums.DeviceType;

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
public class UnifiedDevice implements IDevice<UUID>, UnifiedIoTEntity {

    @NonNull
    @Default
    private final UUID key = UUID.randomUUID();
    @NonNull
    private final DeviceType type;
    @NonNull
    private final String networkId;
    @Default
    private final ParticularData<IDevice> particularData = new ParticularData<>(IDevice.class);

}
