package com.nubeiot.edge.connector.bacnet.mixin;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.json.JsonObject;

import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.util.PropertyValues;

import lombok.NonNull;

public final class PropertyValuesMixin extends PropertyValues implements BACnetMixin {

    private final Map<PropertyIdentifier, Encodable> values = new HashMap<>();

    public PropertyValuesMixin(@NonNull PropertyValues values, boolean includeError) {
        values.forEach(opr -> {
            final Encodable value = values.getNoErrorCheck(opr);
            if (value instanceof ErrorClassAndCode && !includeError) {
                return;
            }
            this.values.put(opr.getPropertyIdentifier(), value);
        });
    }

    @Override
    public JsonObject toJson() {
        return getMapper().convertValue(values, JsonObject.class);
    }

}
