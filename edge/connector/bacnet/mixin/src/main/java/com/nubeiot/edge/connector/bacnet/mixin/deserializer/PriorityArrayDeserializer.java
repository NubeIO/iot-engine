package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import java.util.AbstractMap.SimpleEntry;

import io.github.zero88.utils.Functions;
import io.vertx.core.json.JsonObject;

import com.serotonin.bacnet4j.obj.PropertyTypeDefinition;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PriorityArray;
import com.serotonin.bacnet4j.type.primitive.Null;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class PriorityArrayDeserializer implements EncodableDeserializer<PriorityArray, JsonObject> {

    @NonNull
    private final PropertyTypeDefinition itemDefinition;
    @NonNull
    private final PriorityValueDeserializer itemDeserializer = new PriorityValueDeserializer();

    @Override
    public @NonNull Class<PriorityArray> encodableClass() {
        return PriorityArray.class;
    }

    @Override
    public @NonNull Class<JsonObject> javaClass() {
        return JsonObject.class;
    }

    @Override
    public PriorityArray parse(@NonNull JsonObject value) {
        final PriorityArray array = new PriorityArray();
        value.stream()
             .filter(entry -> Functions.getIfThrow(() -> Functions.toInt().apply(entry.getKey())).isPresent())
             .map(entry -> new SimpleEntry<>(Functions.toInt().apply(entry.getKey()), parseValue(entry.getValue())))
             .forEach(entry -> array.put(entry.getKey(), entry.getValue()));
        return array;
    }

    public Encodable parseValue(Object value) {
        return Functions.getIfThrow(() -> EncodableDeserializer.parse(value, itemDefinition, itemDeserializer))
                        .orElse(Null.instance);
    }

}
