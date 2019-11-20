package com.nubeiot.edge.connector.bacnet.dto;

import java.util.HashMap;

import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.mixin.BACnetMixin;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierSerializer;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

public final class ObjectPropertyValues extends HashMap<ObjectIdentifier, PropertyValuesMixin> implements BACnetMixin {

    public ObjectPropertyValues add(@NonNull PropertyValuesMixin value) {
        this.put(value.getObjId(), value);
        return this;
    }

    @Override
    public JsonObject toJson() {
        return entrySet().stream()
                         .collect(JsonObject::new,
                                  (json, entry) -> json.put(ObjectIdentifierSerializer.serialize(entry.getKey()),
                                                            entry.getValue().toJson()), JsonObject::mergeIn);
    }

}
