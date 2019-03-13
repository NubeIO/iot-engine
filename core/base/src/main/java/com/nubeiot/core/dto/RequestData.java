package com.nubeiot.core.dto;

import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public final class RequestData extends ResponseData {

    @JsonProperty(value = "filter")
    private JsonObject filter;
    @Getter
    private Pagination pagination;

    @JsonCreator
    RequestData(@JsonProperty(value = "headers") JsonObject headers, @JsonProperty(value = "body") JsonObject body,
                @JsonProperty(value = "filter") JsonObject filter,
                @JsonProperty(value = "pagination") Pagination pagination) {
        super(headers, body);
        this.filter = Objects.nonNull(filter) ? filter : new JsonObject();
        this.pagination = pagination;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private JsonObject headers;
        private JsonObject body;
        private JsonObject filter;
        private Pagination pagination;

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
            this.pagination = Objects.isNull(pagination) ? Pagination.builder().build() : pagination;
            return this;
        }

        public RequestData build() {
            return new RequestData(headers, body, filter, pagination);
        }

    }

}
