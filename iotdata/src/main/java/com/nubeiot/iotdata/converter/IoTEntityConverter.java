package com.nubeiot.iotdata.converter;

import com.nubeiot.iotdata.IoTEntity;

/**
 * Represents a {@code converter} between two equivalent types of {@code IoT entity} with different protocol
 *
 * @param <T> Type of IoT entity
 * @param <U> Type of IoT entity
 * @since 1.0.0
 */
public interface IoTEntityConverter<T extends IoTEntity, U> extends IoTConverter<T, U> {

}
