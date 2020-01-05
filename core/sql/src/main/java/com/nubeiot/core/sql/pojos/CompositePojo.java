package com.nubeiot.core.sql.pojos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public interface CompositePojo<P extends VertxPojo, CP extends CompositePojo> extends VertxPojo {

    @SuppressWarnings("unchecked")
    static <M extends VertxPojo, C extends CompositePojo> C create(Object pojo, Class<M> pojoClass, Class<C> clazz) {
        return (C) ReflectionClass.createObject(clazz).wrap(pojoClass.cast(pojo));
    }

    /**
     * Defines extension pojo
     *
     * @return extension pojo
     */
    @NonNull ExtensionPojo extension();

    @NonNull CP wrap(@NonNull P pojo);

    /**
     * Wrap external properties
     *
     * @param other Map external properties
     * @return a reference to this, so the API can be used fluently
     */
    @SuppressWarnings("unchecked")
    default @NonNull CP wrap(@NonNull Map<String, VertxPojo> other) {
        extension().other.putAll(other);
        return (CP) this;
    }

    /**
     * Put external property
     *
     * @param otherKey External key
     * @param pojo     External pojo
     * @return a reference to this, so the API can be used fluently
     */
    @SuppressWarnings("unchecked")
    default @NonNull CP put(String otherKey, @NonNull VertxPojo pojo) {
        extension().other.put(Strings.requireNotBlank(otherKey), pojo);
        return (CP) this;
    }

    /**
     * Put external properties
     *
     * @param otherKey External key
     * @param pojos    list of pojo that same kind
     * @return a reference to this, so the API can be used fluently
     */
    @SuppressWarnings("unchecked")
    default @NonNull CP put(String otherKey, @NonNull List<VertxPojo> pojos) {
        extension().others.put(Strings.requireNotBlank(otherKey), pojos);
        return (CP) this;
    }

    default Object prop(String key) {
        return this.toJson().getValue(Strings.requireNotBlank(key));
    }

    /**
     * Update raw property
     *
     * @param key   Raw key
     * @param value Value
     * @return a reference to this, so the API can be used fluently
     */
    @SuppressWarnings("unchecked")
    default CP with(String key, Object value) {
        return (CP) fromJson(this.toJson().put(Strings.requireNotBlank(key), value));
    }

    @SuppressWarnings("unchecked")
    default <M extends VertxPojo> M getOther(String key) {
        return (M) extension().other.get(Strings.requireNotBlank(key));
    }

    @SuppressWarnings("unchecked")
    default <M extends VertxPojo> M safeGetOther(String key, @NonNull Class<M> clazz) {
        final VertxPojo data = extension().other.get(Strings.requireNotBlank(key));
        return clazz.isInstance(data) ? (M) data : null;
    }

    default List<VertxPojo> getOthers(String key) {
        return extension().others.get(Strings.requireNotBlank(key));
    }

    default JsonObject extensionToJson() {
        return extension().toJson();
    }

    final class ExtensionPojo implements JsonData {

        final @NonNull Map<String, VertxPojo> other = new HashMap<>();

        final @NonNull Map<String, List<VertxPojo>> others = new HashMap<>();

        @Override
        public JsonObject toJson() {
            JsonObject m1 = new JsonObject(
                other.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toJson())));
            JsonObject m2 = new JsonObject(others.entrySet()
                                                 .stream()
                                                 .collect(Collectors.toMap(Entry::getKey, e -> e.getValue()
                                                                                                .stream()
                                                                                                .map(VertxPojo::toJson)
                                                                                                .collect(
                                                                                                    Collectors.toList()))));
            return m1.mergeIn(m2);
        }

    }

}
