package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import io.vertx.core.json.JsonObject;

import com.serotonin.bacnet4j.type.constructed.BaseType;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class BaseTypeDeserializer<T extends BaseType> implements EncodableDeserializer<T, JsonObject> {

    @NonNull
    private final Class<T> clazz;

    @Override
    public @NonNull Class<T> encodableClass() {
        return clazz;
    }

    @Override
    public @NonNull Class<JsonObject> fromClass() {
        return JsonObject.class;
    }

    @Override
    public T parse(@NonNull JsonObject value) {
        return null;
    }

}
