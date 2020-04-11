package com.nubeiot.core.dto;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.zero.utils.Strings;
import io.vertx.core.json.JsonObject;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
        return Arrays.stream(getString(Filters.INCLUDE, "").split(",")).collect(Collectors.toSet());
    }

    private boolean parseBoolean(String pretty) {
        return Boolean.parseBoolean(Strings.toString(this.getValue(pretty)));
    }

    /**
     * Represents for Filters.
     *
     * @since 1.0.0
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Filters {

        /**
         * For {@code advance query}
         *
         * @since 1.0.0
         */
        public static final String QUERY = "_q";

        /**
         * To {@code prettify} response
         *
         * @since 1.0.0
         */
        public static final String PRETTY = "_pretty";

        /**
         * For {@code language}
         *
         * @since 1.0.0
         */
        public static final String LANG = "_lang";

        /**
         * For {@code pagination}
         *
         * @since 1.0.0
         */
        public static final String PAGE = "page";

        /**
         * For {@code pagination}
         *
         * @since 1.0.0
         */
        public static final String PER_PAGE = "per_page";

        /**
         * For {@code audit}
         *
         * @since 1.0.0
         */
        public static final String AUDIT = "_audit";

        /**
         * For {@code audit} in temporary
         *
         * @since 1.0.0
         */
        public static final String TEMP_AUDIT = "_temp_audit";

        /**
         * For {@code sort}
         *
         * @since 1.0.0
         */
        public static final String SORT = "_sort";

        /**
         * For {@code include}
         *
         * @since 1.0.0
         */
        public static final String INCLUDE = "_incl";

        /**
         * For {@code force}
         *
         * @since 1.0.0
         */
        public static final String FORCE = "_force";

    }

}
