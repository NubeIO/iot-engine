package com.nubeiot.core.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestData implements Serializable {

    private Map<String, Object> body;
    private Map<String, Object> filter;
    @Getter
    private Pagination pagination;

    public static Builder builder() {return new Builder();}

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    public JsonObject getBody() {
        return Objects.isNull(this.body) ? new JsonObject() : JsonObject.mapFrom(this.body);
    }

    public JsonObject getFilter() {
        return Objects.isNull(this.filter) ? new JsonObject() : JsonObject.mapFrom(this.filter);
    }

    public static class Builder {

        private JsonObject body;
        private JsonObject filter;
        private Pagination pagination = Pagination.builder().build();

        public Builder body(JsonObject body) {
            this.body = body;
            return this;
        }

        public Builder filter(JsonObject filter) {
            this.filter = filter;
            return this;
        }

        public Builder pagination(Pagination pagination) {
            this.pagination = Objects.isNull(pagination) ? this.pagination : pagination;
            return this;
        }

        public RequestData build() {
            return new RequestData(Objects.isNull(body) ? null : body.getMap(),
                                   Objects.isNull(filter) ? null : filter.getMap(), pagination);
        }

    }

}
