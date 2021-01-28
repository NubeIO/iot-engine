package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;

import lombok.NonNull;

/**
 * @see PropertyValues
 * @see PropertyValuesMixin
 */
public final class ObjectPropertyValues implements BACnetJsonMixin {

    private final Map<ObjectIdentifier, PropertyValuesMixin> values = new HashMap<>();

    public ObjectPropertyValues add(@NonNull ObjectIdentifier objId, @NonNull PropertyValuesMixin value) {
        values.put(objId, value);
        return this;
    }

    @Override
    public JsonObject toJson() {
        return getMapper().convertValue(values, JsonObject.class);
    }

    public static final class ObjectPropertyValuesSerializer extends StdSerializer<ObjectPropertyValues> {

        ObjectPropertyValuesSerializer() {
            super(ObjectPropertyValues.class);
        }

        @Override
        public void serialize(ObjectPropertyValues value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
            gen.writeObject(value.toJson());
        }

    }

}
