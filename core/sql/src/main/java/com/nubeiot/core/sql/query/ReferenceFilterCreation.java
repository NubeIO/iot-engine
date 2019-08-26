package com.nubeiot.core.sql.query;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface ReferenceFilterCreation {

    static JsonObject createFilter(@NonNull List<EntityMetadata> references, JsonObject filter) {
        if (Objects.isNull(filter)) {
            return new JsonObject();
        }
        return createFilter(
            references.stream().collect(Collectors.toMap(EntityMetadata::singularKeyName, Function.identity())),
            filter);
    }

    static JsonObject createFilter(@NonNull Map<String, EntityMetadata> references, JsonObject filter) {
        if (Objects.isNull(filter)) {
            return new JsonObject();
        }
        filter.getMap()
              .putAll(filter.fieldNames()
                            .stream()
                            .filter(s -> s.contains(".") && references.containsKey(s.substring(0, s.indexOf("."))))
                            .map(ReferenceFilterCreation::refKeyEntryWithRoot)
                            .map(entry -> referenceFilterEntry(filter, entry))
                            .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
        return filter;
    }

    static Entry<String, String> refKeyEntryWithRoot(String s) {
        return new SimpleEntry<>(s.substring(0, s.indexOf(".")), s);
    }

    static Entry<String, Map<String, Object>> referenceFilterEntry(JsonObject rootFilter, Entry<String, String> entry) {
        return new SimpleEntry<>(entry.getKey(), findRefFilterFromRoot(rootFilter, entry));
    }

    static Map<String, Object> findRefFilterFromRoot(JsonObject rootFilter, Entry<String, String> entry) {
        return Collections.singletonMap(entry.getValue().replaceAll("^" + entry.getKey() + "\\.", ""),
                                        rootFilter.getValue(entry.getValue()));
    }

}
