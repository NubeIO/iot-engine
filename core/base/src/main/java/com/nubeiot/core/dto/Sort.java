package com.nubeiot.core.dto;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

@Builder(builderClassName = "Builder")
public final class Sort implements Serializable, JsonData {

    @Singular
    private final Map<String, SortType> items;

    public static Sort from(String requestParam) {
        if (Strings.isBlank(requestParam)) {
            return null;
        }
        return Sort.builder()
                   .items(Stream.of(requestParam.split(","))
                                .filter(Strings::isNotBlank)
                                .map(Sort::each)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toMap(Entry::getKey, Entry::getValue)))
                   .build();
    }

    @JsonCreator
    static Sort create(@NonNull Map<String, String> data) {
        return Sort.builder()
                   .items(data.entrySet()
                              .stream()
                              .map(Sort::each)
                              .filter(Objects::nonNull)
                              .collect(Collectors.toMap(Entry::getKey, Entry::getValue)))
                   .build();
    }

    private static Entry<String, SortType> each(String value) {
        if (Strings.isBlank(value)) {
            return null;
        }
        final char c = value.charAt(0);
        SortType type = c == SortType.DESC.getSymbol() ? SortType.DESC : SortType.ASC;
        String resource = c == SortType.ASC.getSymbol() || type == SortType.DESC ? value.substring(1) : value;
        return new SimpleEntry<>(resource, type);
    }

    private static Entry<String, SortType> each(@NonNull Entry<String, String> entry) {
        if (Strings.isBlank(entry.getKey())) {
            return null;
        }
        SortType type = SortType.parse(entry.getValue());
        if (type == null) {
            return null;
        }
        return new SimpleEntry<>(entry.getKey(), type);
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public JsonObject toJson() {
        return JsonData.MAPPER.convertValue(items, JsonObject.class);
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum SortType {
        ASC('+'), DESC('-');

        private final char symbol;

        public static SortType parse(String type) {
            if (Strings.isBlank(type)) {
                return ASC;
            }
            if (type.length() == 1) {
                return type.charAt(0) == DESC.symbol ? DESC : ASC;
            }
            return Stream.of(SortType.values()).filter(t -> t.name().equalsIgnoreCase(type)).findFirst().orElse(null);
        }
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
