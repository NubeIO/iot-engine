package com.nubeiot.core.sql.query;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.sql.EntityMetadata;

import lombok.NonNull;

public interface QueryParser {

    static JsonObject fromReference(@NonNull EntityMetadata reference, JsonObject root) {
        if (Objects.isNull(root)) {
            return new JsonObject();
        }
        return streamRefs(reference, root).collect(JsonObject::new,
                                                   (json, entry) -> json.put(entry.getKey(), entry.getValue()),
                                                   (json1, json2) -> json1.mergeIn(json2, true));
    }

    static Entry<String, String> refKeyEntryWithRoot(String s) {
        return new SimpleEntry<>(s.substring(0, s.indexOf('.')), s);
    }

    static Entry<String, Object> findRef(@NonNull JsonObject filter, Entry<String, String> entry) {
        return new SimpleEntry<>(entry.getValue().replaceAll("^" + entry.getKey() + "\\.", ""),
                                 filter.getValue(entry.getValue()));
    }

    static Stream<Entry<String, Object>> streamRefs(@NonNull EntityMetadata reference, @NonNull JsonObject root) {
        return root.fieldNames()
                   .stream()
                   .filter(s -> s.contains("."))
                   .filter(s -> reference.singularKeyName().equals(s.substring(0, s.indexOf('.'))))
                   .map(QueryParser::refKeyEntryWithRoot)
                   .map(entry -> findRef(root, entry));
    }

}
