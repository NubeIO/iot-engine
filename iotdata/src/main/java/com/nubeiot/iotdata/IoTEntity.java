package com.nubeiot.iotdata;

import io.github.zero88.qwe.dto.JsonData;

import lombok.NonNull;

/**
 * Represents for an {@code IoT virtual entity}
 *
 * @param <K> Type of entity key
 */
public interface IoTEntity<K> extends IoTObject, HasProtocol, JsonData {

    /**
     * Gets entity key
     *
     * @return entity key
     */
    @NonNull K key();

}
