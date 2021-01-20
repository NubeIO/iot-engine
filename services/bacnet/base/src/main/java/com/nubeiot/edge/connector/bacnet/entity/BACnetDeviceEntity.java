package com.nubeiot.edge.connector.bacnet.entity;

import com.nubeiot.iotdata.entity.IDevice;
import com.nubeiot.iotdata.enums.DeviceType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class BACnetDeviceEntity implements BACnetEntity<String>, IDevice<String> {

    private final String key;
    private final DeviceType type;

}
