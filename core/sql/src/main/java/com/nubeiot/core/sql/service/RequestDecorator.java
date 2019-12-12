package com.nubeiot.core.sql.service;

import com.nubeiot.core.dto.RequestData;

import lombok.NonNull;

/**
 * Represents decorator that make up {@code request data} depends on {@code entity resource}
 *
 * @since 1.0.0
 */
public interface RequestDecorator {

    /**
     * Decorates request data on creating one resource.
     *
     * @param requestData the request data
     * @return the request data
     * @since 1.0.0
     */
    @NonNull
    default RequestData onCreatingOneResource(@NonNull RequestData requestData) {
        return requestData;
    }

    /**
     * Decorates request data on modifying one resource.
     *
     * @param requestData the request data
     * @return the request data
     * @since 1.0.0
     */
    @NonNull
    default RequestData onModifyingOneResource(@NonNull RequestData requestData) {
        return requestData;
    }

    /**
     * Decorates request data on reading many resource.
     *
     * @param requestData the request data
     * @return the request data
     * @since 1.0.0
     */
    @NonNull
    default RequestData onReadingManyResource(@NonNull RequestData requestData) {
        return requestData;
    }

    /**
     * Decorates request data on reading one resource request data.
     *
     * @param requestData the request data
     * @return the request data
     * @since 1.0.0
     */
    @NonNull
    default RequestData onReadingOneResource(@NonNull RequestData requestData) {
        return requestData;
    }

}
