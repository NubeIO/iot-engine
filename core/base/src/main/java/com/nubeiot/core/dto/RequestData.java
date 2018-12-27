package com.nubeiot.core.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class RequestData implements Serializable, JsonData {

    private Map<String, Object> body;
    private Map<String, Object> filter;
    @Getter
    private Pagination pagination;

    public static Builder builder() {return new Builder();}

    public JsonObject getBody() {
        return Objects.isNull(this.body) ? new JsonObject() : JsonObject.mapFrom(this.body);
    }

    public JsonObject getFilter() {
        return Objects.isNull(this.filter) ? new JsonObject() : JsonObject.mapFrom(this.filter);
    }

    public static class Builder {

        private JsonObject body;
        private JsonObject filter;
        private Pagination pagination;

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
            return new RequestData(Objects.isNull(body) ? null : body.getMap(),
                                   Objects.isNull(filter) ? null : filter.getMap(), pagination);
        }

    }

}
