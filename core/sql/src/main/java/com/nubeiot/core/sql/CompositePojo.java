package com.nubeiot.core.sql;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public interface CompositePojo<T extends VertxPojo> extends VertxPojo {

    @NonNull Map<String, VertxPojo> other();

    @SuppressWarnings("unchecked")
    default T unwrap() {
        return (T) this;
    }

    default CompositePojo wrap(Map<String, VertxPojo> other) {
        other().putAll(other);
        return this;
    }

    @SuppressWarnings("unchecked")
    default <P extends VertxPojo> P get(String key) {
        return (P) other().get(Strings.requireNotBlank(key));
    }

    @SuppressWarnings("unchecked")
    default <P extends VertxPojo> P safeGet(String key, @NonNull Class<P> clazz) {
        final VertxPojo data = other().get(Strings.requireNotBlank(key));
        return clazz.isInstance(data) ? (P) data : null;
    }

    default JsonObject getJson(String key) {
        return Optional.ofNullable(other().get(Strings.requireNotBlank(key))).map(VertxPojo::toJson).orElse(null);
    }

    default JsonObject otherToJson() {
        return new JsonObject(
            other().entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toJson())));
    }

}
