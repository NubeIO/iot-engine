package com.nubeiot.core.sql;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents {@code resource} has one or more {@code reference} to other resources.
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

}
