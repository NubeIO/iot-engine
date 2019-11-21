package com.nubeiot.edge.connector.bacnet.dto;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.mixin.BACnetMixin;
import com.nubeiot.edge.connector.bacnet.mixin.EncodableSerializer;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.error.ErrorClassAndCode;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertyValuesMixin implements BACnetMixin {

    @Getter
    private final ObjectIdentifier objId;
    private final Map<PropertyIdentifier, Encodable> values;

    public static PropertyValuesMixin create(@NonNull ObjectIdentifier objId, @NonNull PropertyValues values,
                                             boolean includeError) {
        final Map<PropertyIdentifier, Encodable> map = new HashMap<>();
        values.forEach(opr -> {
            if (!opr.getObjectIdentifier().equals(objId) ||
                PropertyIdentifier.propertyList.equals(opr.getPropertyIdentifier())) {
                return;
            }
            final Encodable value = values.getNoErrorCheck(opr);
            if (value instanceof ErrorClassAndCode && !includeError) {
                return;
            }
            map.put(opr.getPropertyIdentifier(), value);
        });
        return new PropertyValuesMixin(objId, map);
    }

    public Encodable get(@NonNull PropertyIdentifier propertyIdentifier) {
        return values.get(propertyIdentifier);
    }

    public <T> T getAndCast(@NonNull PropertyIdentifier propertyIdentifier) {
        return EncodableSerializer.encode(get(propertyIdentifier));
    }

    @Override
    public JsonObject toJson() {
        return getMapper().convertValue(values, JsonObject.class);
    }

}
