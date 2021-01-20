package com.nubeiot.edge.connector.bacnet.entity;

import com.nubeiot.iotdata.entity.IPoint;
import com.nubeiot.iotdata.enums.PointType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class BACnetPointEntity implements BACnetEntity<String>, IPoint<String> {

    private final String key;
    private final PointType type;

}
