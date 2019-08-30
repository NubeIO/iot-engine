package com.nubeiot.core.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(builderClassName = "Builder")
@JsonInclude(Include.NON_EMPTY)
@JsonDeserialize(builder = SortItem.Builder.class)
public class SortItem implements Serializable, JsonData {

    @NonNull
    private final SortType type;
    @NonNull
    private final String resource;
    @Default
    private final String group = "";

    public static SortItem from(String value) {
        if (Strings.isBlank(value)) {
            return null;
        }
        final char c = value.charAt(0);
        SortType type = c == SortType.DESC.getSymbol() ? SortType.DESC : SortType.ASC;
        String resource = c == SortType.ASC.getSymbol() || type == SortType.DESC ? value.substring(1) : value;
        String group = !resource.contains(".") ? "" : resource.substring(0, resource.indexOf("."));
        return SortItem.builder()
                       .type(type)
                       .group(group)
                       .resource(Strings.isBlank(group) ? resource : resource.substring(resource.indexOf(".") + 1))
                       .build();
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum SortType {
        ASC('+'), DESC('-');

        private final char symbol;
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
