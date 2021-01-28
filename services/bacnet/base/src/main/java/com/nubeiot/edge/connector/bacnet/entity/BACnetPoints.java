package com.nubeiot.edge.connector.bacnet.entity;

import java.util.HashMap;
import java.util.Map;

import io.github.zero88.qwe.dto.JsonData;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.NonNull;

public final class BACnetPoints implements JsonData {

    private final Map<ObjectIdentifier, BACnetPointEntity> values = new HashMap<>();

    public BACnetPoints add(@NonNull ObjectIdentifier objId, @NonNull BACnetPointEntity value) {
        values.put(objId, value);
        return this;
    }

    @Override
    public JsonObject toJson() {
        return values.entrySet()
                     .stream()
                     .collect(JsonObject::new,
                              (json, entry) -> json.put(ObjectIdentifierMixin.serialize(entry.getKey()),
                                                        entry.getValue().toJson()), (json1, json2) -> {});
    }

}
