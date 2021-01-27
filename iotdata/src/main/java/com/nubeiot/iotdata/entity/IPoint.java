package com.nubeiot.iotdata.entity;

import com.nubeiot.iotdata.IoTEntity;
import com.nubeiot.iotdata.enums.PointType;

/**
 * Represents for a {@code semantic IoT point} entity
 *
 * @param <K> Type of point key
 */
public interface IPoint<K> extends IoTEntity<K>, HasObjectType<PointType> {}
