package com.nubeiot.iotdata.entity;

import java.util.UUID;

import com.nubeiot.iotdata.UnifiedIoTEntity;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@SuperBuilder
@Accessors(fluent = true)
public class UnifiedDevice extends AbstractDevice<UUID> implements UnifiedIoTEntity {

    @NonNull
    @Default
    private final ParticularData<IDevice> particularData = new ParticularData<>(IDevice.class);

}
