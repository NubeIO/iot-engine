package com.nubeiot.core.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

@Builder(builderClassName = "Builder")
public final class Sort implements Serializable, JsonData {

    @Singular
    private final Map<String, List<SortItem>> items;

    public static Sort from(String requestParam) {
        if (Strings.isBlank(requestParam)) {
            return null;
        }
        return Sort.builder()
                   .items(Stream.of(requestParam.split(","))
                                .filter(Strings::isNotBlank)
                                .map(SortItem::from)
                                .filter(Objects::nonNull)
                                .collect(Collectors.groupingBy(SortItem::getGroup)))
                   .build();
    }

    @JsonCreator
    static Sort create(@NonNull Map<String, List<Object>> map) {
        return Sort.builder()
                   .items(map.entrySet()
                             .stream()
                             .collect(Collectors.toMap(Entry::getKey, e -> e.getValue()
                                                                            .stream()
                                                                            .map(o -> JsonData.from(o, SortItem.class))
                                                                            .collect(Collectors.toList()))))
                   .build();
    }

    @Override
    public JsonObject toJson() {
        return items.entrySet()
                    .stream()
                    .collect(JsonObject::new, (json, node) -> json.put(node.getKey(), node.getValue()),
                             (json1, json2) -> json2.mergeIn(json1, true));
    }

}
