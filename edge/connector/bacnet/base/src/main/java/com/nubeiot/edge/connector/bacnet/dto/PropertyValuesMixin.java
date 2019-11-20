package com.nubeiot.edge.connector.bacnet.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.mixin.BACnetMixin;
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

    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull PropertyIdentifier propertyIdentifier) {
        final Encodable value = values.get(propertyIdentifier);
        if (Objects.isNull(value)) {
            return null;
        }
        return (T) BACnetMixin.MAPPER.convertValue(Collections.singletonMap(propertyIdentifier, value),
                                                   JsonObject.class)
                                     .stream()
                                     .map(Entry::getValue)
                                     .findFirst()
                                     .orElse(null);
    }

    @Override
    public JsonObject toJson() {
        return getMapper().convertValue(values, JsonObject.class);
    }

}
