package com.nubeiot.core.dto;

import io.github.zero88.jpa.Pageable;
import io.github.zero88.utils.Strings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.RequestFilter.Filters;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Pagination implements Pageable, JsonData {

    @JsonProperty(Filters.PAGE)
    private int page;
    @JsonProperty(Filters.PER_PAGE)
    private int perPage;

    public static Pagination oneValue() {
        return Pagination.builder().perPage(1).page(1).build();
    }

    public static Builder builder() { return new Builder(); }

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
