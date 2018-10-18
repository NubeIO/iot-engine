package com.nubeio.iot.share.event;

import java.util.Objects;

import io.vertx.core.json.JsonObject;
import lombok.Getter;

@Getter
public class RequestData {

    private final JsonObject body;
    private final JsonObject filter;
    private final Pagination pagination;

    RequestData(JsonObject body, JsonObject filter, Pagination pagination) {
        this.body = body;
        this.filter = filter;
        this.pagination = pagination;
    }

    public static Builder builder() {return new Builder();}

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
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
            return new RequestData(body, filter, pagination);
        }

    }

}
