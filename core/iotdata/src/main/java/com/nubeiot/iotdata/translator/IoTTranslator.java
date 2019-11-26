package com.nubeiot.iotdata.translator;

import java.io.Serializable;

import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

/**
 * Represents {@code translator} between {@code Nube IoT concept} object and the equivalent concept object in another
 * {@code protocol}
 *
 * @param <T> Nube IoT concept type
 * @param <U> Protocol object type
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
     * Translate a {@code protocol} object to a {@code Nube IoT concept}
     *
     * @param object {@code protocol} object
     * @return The Nube IoT concept
     * @apiNote if cannot translate, output can be {@code null} or throw {@link IllegalArgumentException} depends on
     *     detail implementation
     */
    T serialize(U object);

    /**
     * Translate {@code Nube IoT concept} to a {@code protocol} object
     *
     * @param concept The Nube IoT concept
     * @return The protocol object
     * @apiNote if cannot translate, output can be {@code null} or throw {@link IllegalArgumentException} depends on
     *     detail implementation
     */
    U deserialize(T concept);

    /**
     * The {@code Nube IoT concept} type
     */
    @NonNull Class<T> fromType();

    /**
     * The {@code protocol} object type
     */
    @NonNull Class<U> toType();

}
