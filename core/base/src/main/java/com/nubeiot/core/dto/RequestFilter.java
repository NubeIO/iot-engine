package com.nubeiot.core.dto;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class RequestFilter extends JsonObject implements JsonData {

    public RequestFilter(JsonObject filter) {
        super(filter.copy().getMap());
    }

    public boolean isPretty() {
        return parseBoolean(Filters.PRETTY);
    }

    public boolean hasForce() {
        return parseBoolean(Filters.FORCE);
    }

    public boolean hasAudit() {
        return parseBoolean(Filters.AUDIT);
    }

    public boolean hasTempAudit() {
        return parseBoolean(Filters.TEMP_AUDIT);
    }

    public Set<String> getIncludes() {
        return Arrays.stream(getString(RequestFilter.Filters.INCLUDE, "").split(",")).collect(Collectors.toSet());
    }

    private boolean parseBoolean(String pretty) {
        return Boolean.parseBoolean(Strings.toString(this.getValue(pretty)));
    }

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
        //TODO handle force delete
        public static final String FORCE = "_force";

    }

}
