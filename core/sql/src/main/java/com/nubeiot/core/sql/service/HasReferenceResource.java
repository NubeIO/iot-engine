package com.nubeiot.core.sql.service;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

import com.nubeiot.core.dto.JsonData;

/**
 * Mark to {@code EntityService} to represent {@code resource} has one or more {@code reference} to other resources.
 *
 * @see EntityService
 */
public interface HasReferenceResource {

    /**
     * Defines mapping between {@code json request field} in {@code request body} and {@code database field}
     *
     * @return a mapping between {@code json request field} and {@code database field}
     */
    default Map<String, String> jsonRefFields() {
        return Collections.emptyMap();
    }

    /**
     * Defines mapping between {@code json} request field in {@code request body} and converter function from {@code
     * request value} to {@code actual database value}. The converter output function will be used when overriding in
     * filter
     *
     * @return a mapping between {@code json request field} and {@code value converter}
     */
    Map<String, Function<String, ?>> jsonFieldConverter();

    default Function<Entry<String, Object>, String> keyMapper() {
        return e -> jsonRefFields().getOrDefault(e.getKey(), e.getKey());
    }

    default Function<Entry<String, Object>, Object> valueMapper() {
        return e -> Objects.isNull(e.getValue())
                    ? null
                    : JsonData.checkAndConvert(jsonFieldConverter().get(e.getKey()).apply(e.getValue().toString()));
    }

}
