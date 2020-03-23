package com.nubeiot.core.dto;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Represents for Request filter.
 *
 * @since 1.0.0
 */
@NoArgsConstructor
public final class RequestFilter extends JsonObject implements JsonData {

    /**
     * Instantiates a new Request filter.
     *
     * @param filter the filter
     * @since 1.0.0
     */
    public RequestFilter(JsonObject filter) {
        super(filter.copy().getMap());
    }

    /**
     * Is pretty.
     *
     * @return the boolean
     * @since 1.0.0
     */
    public boolean isPretty() {
        return parseBoolean(Filters.PRETTY);
    }

    /**
     * Has force.
     *
     * @return the boolean
     * @see Filters#FORCE
     * @since 1.0.0
     */
    public boolean hasForce() {
        return parseBoolean(Filters.FORCE);
    }

    /**
     * Has audit.
     *
     * @return the boolean
     * @since 1.0.0
     */
    public boolean hasAudit() {
        return parseBoolean(Filters.AUDIT);
    }

    /**
     * Has temp audit.
     *
     * @return the boolean
     * @since 1.0.0
     */
    public boolean hasTempAudit() {
        return parseBoolean(Filters.TEMP_AUDIT);
    }

    /**
     * Gets includes.
     *
     * @return the includes
     * @since 1.0.0
     */
    public Set<String> getIncludes() {
        return Arrays.stream(getString(RequestFilter.Filters.INCLUDE, "").split(",")).collect(Collectors.toSet());
    }

    public boolean parseBoolean(@NonNull String param) {
        return Boolean.parseBoolean(Strings.toString(this.getValue(param)));
    }

    /**
     * Represents for Filters.
     *
     * @since 1.0.0
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Filters {

        /**
         * To {@code prettify} response
         */
        public static final String PRETTY = "_pretty";

        /**
         * For {@code language}
         */
        public static final String LANG = "_lang";

        /**
         * For {@code pagination}
         */
        public static final String PAGE = "page";

        /**
         * For {@code pagination}
         */
        public static final String PER_PAGE = "per_page";

        /**
         * For {@code audit}
         */
        public static final String AUDIT = "_audit";

        /**
         * For {@code audit} in temporary
         */
        public static final String TEMP_AUDIT = "_temp_audit";

        /**
         * For {@code sort}
         */
        public static final String SORT = "_sort";

        /**
         * For {@code include}
         */
        public static final String INCLUDE = "_incl";

        /**
         * For {@code force}
         */
        public static final String FORCE = "_force";

    }

}
