package com.nubeiot.iotdata.translator;

import java.io.Serializable;

import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

/**
 * Represents {@code translator} between {@code Nube IoT concept} object and the equivalent concept object in another
 * {@code protocol}
 *
 * @param <T> Nube IoT concept type
 * @param <U> Protocol type
 * @see Protocol
 */
public interface IoTTranslator<T, U> extends Serializable {

    /**
     * Defines translator for which protocol
     *
     * @return protocol
     * @see Protocol
     */
    @NonNull Protocol protocol();

    /**
     * Translate {@code Nube IoT concept} to a {@code protocol} object
     *
     * @param concept The Nube IoT concept
     * @return The protocol object
     */
    U from(T concept);

    /**
     * Translate a {@code protocol} object to a {@code Nube IoT concept}
     *
     * @param object {@code protocol} object
     * @return The Nube IoT concept
     */
    T to(U object);

    /**
     * The {@code Nube IoT concept} type
     */
    Class<T> fromType();

    /**
     * The {@code protocol} object type
     */
    Class<U> toType();

}
