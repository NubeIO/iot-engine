package com.nubeiot.core.sql;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents {@code resource} has one or more {@code reference} to other resources.
 */
public interface HasReferenceResource {

    /**
     * Defines mapping between json request field in {@code request body} and {@code actual field} in database
     *
     * @return map field
     */
    default Map<String, String> jsonFields() {
        return Collections.emptyMap();
    }

    /**
     * Defines mapping between json request field in {@code request body} and converter request value to actual database
     * value. It will be used when overriding filter
     *
     * @return map field
     */
    Map<String, Function<String, ?>> extensions();

}
