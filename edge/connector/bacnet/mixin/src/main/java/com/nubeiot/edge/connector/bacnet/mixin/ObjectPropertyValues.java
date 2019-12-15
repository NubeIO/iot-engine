package com.nubeiot.edge.connector.bacnet.mixin;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.json.JsonObject;

import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;

import lombok.NonNull;

/**
 * @see PropertyValues
 * @see PropertyValuesMixin
 */
public final class ObjectPropertyValues implements BACnetMixin {

    private final Map<ObjectIdentifier, PropertyValuesMixin> values = new HashMap<>();

    public ObjectPropertyValues add(@NonNull ObjectIdentifier objId, @NonNull PropertyValuesMixin value) {
        values.put(objId, value);
        return this;
    }

    @Override
    public JsonObject toJson() {
        return getMapper().convertValue(values, JsonObject.class);
    }

}
