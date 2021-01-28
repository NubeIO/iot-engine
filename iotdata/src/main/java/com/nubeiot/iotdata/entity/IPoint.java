package com.nubeiot.iotdata.entity;

import com.nubeiot.iotdata.IoTEntity;
import com.nubeiot.iotdata.enums.PointType;

import lombok.NonNull;

/**
 * Represents for a {@code semantic IoT point} entity
 *
 * @param <K> Type of point key
 */
public interface IPoint<K> extends IoTEntity<K>, HasObjectType<PointType> {

    /**
     * Retrieve a network identifier that point belongs to
     *
     * @return network identifier
     */
    @NonNull String networkId();

    /**
     * Retrieve a device identifier that point belongs to
     *
     * @return network identifier
     */
    @NonNull String deviceId();

}
