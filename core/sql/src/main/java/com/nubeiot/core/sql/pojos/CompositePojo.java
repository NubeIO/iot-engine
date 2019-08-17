package com.nubeiot.core.sql.pojos;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public interface CompositePojo<P extends VertxPojo, CP extends CompositePojo> extends VertxPojo {

    @NonNull Map<String, VertxPojo> other();

    @NonNull Class<P> pojoClass();

    default P unwrap() {
        return pojoClass().cast(this);
    }

    @SuppressWarnings("unchecked")
    default CP wrap(Map<String, VertxPojo> other) {
        other().putAll(other);
        return (CP) this;
    }

    @SuppressWarnings("unchecked")
    default <M extends VertxPojo> M get(String key) {
        return (M) other().get(Strings.requireNotBlank(key));
    }

    @SuppressWarnings("unchecked")
    default <M extends VertxPojo> M safeGet(String key, @NonNull Class<M> clazz) {
        final VertxPojo data = other().get(Strings.requireNotBlank(key));
        return clazz.isInstance(data) ? (M) data : null;
    }

    default JsonObject getJson(String key) {
        return Optional.ofNullable(other().get(Strings.requireNotBlank(key))).map(VertxPojo::toJson).orElse(null);
    }

    default JsonObject otherToJson() {
        return new JsonObject(
            other().entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toJson())));
    }

}
