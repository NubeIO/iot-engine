package com.nubeiot.core.dto;

import java.io.Serializable;

import com.nubeiot.core.utils.Strings;

import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Pagination implements Serializable {

    private int page;
    private int perPage;

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    public static Builder builder() {return new Builder();}

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private static final int DEFAULT_PER_PAGE = 20;
        private static final int DEFAULT_PAGE = 1;

        private int page = DEFAULT_PAGE;
        private int perPage = DEFAULT_PER_PAGE;

        public Builder page(int page) {
            this.page = Math.max(DEFAULT_PAGE, page);
            return this;
        }

        public Builder page(String page) {
            this.page = Math.max(DEFAULT_PAGE, Strings.convertToInt(page, DEFAULT_PAGE));
            return this;
        }

        public Builder perPage(int perPage) {
            this.perPage = perPage > 0 ? Math.min(perPage, DEFAULT_PER_PAGE) : DEFAULT_PER_PAGE;
            return this;
        }

        public Builder perPage(String perPage) {
            final int pp = Strings.convertToInt(perPage, DEFAULT_PER_PAGE);
            this.perPage = pp > 0 ? Math.min(pp, DEFAULT_PER_PAGE) : DEFAULT_PER_PAGE;
            return this;
        }

        public Pagination build() {
            return new Pagination(page, perPage);
        }

    }

}
