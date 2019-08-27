package com.nubeiot.core.sql.pojos;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public interface CompositePojo<P extends VertxPojo, CP extends CompositePojo> extends VertxPojo {

    @SuppressWarnings("unchecked")
    static <M extends VertxPojo, C extends CompositePojo> C create(Object pojo, Class<M> pojoClass, Class<C> clazz) {
        return (C) ReflectionClass.createObject(clazz).wrap(pojoClass.cast(pojo));
    }

    @NonNull Map<String, VertxPojo> other();

    CP wrap(@NonNull P pojo);

    @SuppressWarnings("unchecked")
    default CP wrap(@NonNull Map<String, VertxPojo> other) {
        other().putAll(other);
        return (CP) this;
    }

    @SuppressWarnings("unchecked")
    default CP put(String otherKey, @NonNull VertxPojo other) {
        other().put(Strings.requireNotBlank(otherKey), other);
        return (CP) this;
    }

    default Object prop(String key) {
        return this.toJson().getValue(Strings.requireNotBlank(key));
    }

    @SuppressWarnings("unchecked")
    default CP with(String key, Object value) {
        return (CP) fromJson(this.toJson().put(Strings.requireNotBlank(key), value));
    }

    @SuppressWarnings("unchecked")
    default <M extends VertxPojo> M getOther(String key) {
        return (M) other().get(Strings.requireNotBlank(key));
    }

    @SuppressWarnings("unchecked")
    default <M extends VertxPojo> M safeGetOther(String key, @NonNull Class<M> clazz) {
        final VertxPojo data = other().get(Strings.requireNotBlank(key));
        return clazz.isInstance(data) ? (M) data : null;
    }

    default JsonObject otherToJson() {
        return new JsonObject(
            other().entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toJson())));
    }

}
