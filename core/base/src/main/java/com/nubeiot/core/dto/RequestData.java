package com.nubeiot.core.dto;

import java.util.Objects;
import java.util.Optional;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.DataTransferObject.AbstractDTO;
import com.nubeiot.core.event.EventMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
public final class RequestData extends AbstractDTO {

    @JsonProperty(value = "filter")
    private JsonObject filter;
    private Pagination pagination;
    private Sort sort;

    @JsonCreator
    private RequestData(@JsonProperty(value = "headers") JsonObject headers,
                        @JsonProperty(value = "body") JsonObject body,
                        @JsonProperty(value = "filter") JsonObject filter,
                        @JsonProperty(value = "pagination") Pagination pagination,
                        @JsonProperty(value = "sort") Sort sort) {
        super(headers, body);
        this.filter = Objects.nonNull(filter) ? filter : new JsonObject();
        this.pagination = pagination;
        this.sort = sort;
    }

    public boolean hasAudit() {
        return Optional.ofNullable(this.getFilter()).map(o -> o.containsKey(Filters.AUDIT)).orElse(false);
    }

    @Override
    public JsonObject toJson() {
        if (Objects.isNull(sort) || sort.isEmpty()) {
            return JsonData.MAPPER.convertValue(this, JsonObject.class);
        }
        return JsonData.MAPPER.convertValue(this, JsonObject.class).put("sort", sort.toJson());
    }

    public static Builder builder() { return new Builder(); }

    public static RequestData from(@NonNull EventMessage msg) {
        return builder().body(msg.getData()).build();
    }

    public static class Builder {

        private JsonObject headers;
        private JsonObject body;
        private JsonObject filter;
        private Pagination pagination;
        private Sort sort;

        public Builder headers(JsonObject headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(JsonObject body) {
            this.body = body;
            return this;
        }

        public Builder filter(JsonObject filter) {
            this.filter = filter;
            return this;
        }

        public Builder pagination(Pagination pagination) {
            this.pagination = pagination;
            return this;
        }

        public Builder sort(Sort sort) {
            this.sort = sort;
            return this;
        }

        public RequestData build() {
            return new RequestData(headers, body, filter, pagination, sort);
        }

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

    }

}
