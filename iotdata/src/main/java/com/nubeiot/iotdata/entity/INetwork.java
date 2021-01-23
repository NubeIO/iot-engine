package com.nubeiot.iotdata.entity;

import java.util.Collection;
import java.util.Collections;

import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents for a {@code semantic IoT network} entity
 *
 * @param <K> Type of network key
 */
public interface INetwork<K> extends IoTEntity<K> {

    /**
     * Return list of devices that belongs to network
     *
     * @return list of devices, might empty
     * @see IDevice
     */
    default @NonNull Collection<IDevice> devices() {
        return Collections.emptyList();
    }

}
