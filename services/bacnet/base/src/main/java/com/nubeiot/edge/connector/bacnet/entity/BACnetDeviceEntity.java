package com.nubeiot.edge.connector.bacnet.entity;

import com.nubeiot.iotdata.entity.IDevice;
import com.nubeiot.iotdata.enums.DeviceType;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@Accessors(fluent = true)
public class BACnetDeviceEntity implements BACnetEntity<String>, IDevice<String> {

    private final String key;
    private final DeviceType type;
    private final String networkId;

}
