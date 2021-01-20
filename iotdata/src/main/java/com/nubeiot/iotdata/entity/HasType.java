package com.nubeiot.iotdata.entity;

import lombok.NonNull;

/**
 * @param <T> Type of IoT object type
 */
public interface HasType<T> {

    @NonNull T type();

}
