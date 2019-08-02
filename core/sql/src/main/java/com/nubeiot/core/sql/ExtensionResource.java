package com.nubeiot.core.sql;

import java.util.Map;
import java.util.function.Function;

/**
 * Represents a sub path of one or more another {@code resource}.
 */
public interface ExtensionResource {

    /**
     * Defines mapping between json request field in {@code request body} and converter request value to actual database
     * value. It will be used when overriding filter
     *
     * @return map field
     */
    Map<String, Function<String, ?>> extensions();

}
