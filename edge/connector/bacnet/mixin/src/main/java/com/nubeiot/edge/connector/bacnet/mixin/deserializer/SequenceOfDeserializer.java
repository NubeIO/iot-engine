package com.nubeiot.edge.connector.bacnet.mixin.deserializer;

import java.util.Optional;

import io.github.zero.utils.Functions;
import io.vertx.core.json.JsonArray;

import com.serotonin.bacnet4j.obj.PropertyTypeDefinition;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class SequenceOfDeserializer implements EncodableDeserializer<SequenceOf, JsonArray> {

    @NonNull
    private final PropertyTypeDefinition itemDefinition;
    @NonNull
    private final EncodableDeserializer itemDeserializer;

    @Override
    public @NonNull Class<SequenceOf> encodableClass() {
        return SequenceOf.class;
    }

    @Override
    public @NonNull Class<JsonArray> fromClass() {
        return JsonArray.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SequenceOf parse(@NonNull JsonArray array) {
        SequenceOf sequenceOf = new SequenceOf();
        array.stream()
             .map(i -> Functions.getIfThrow(() -> EncodableDeserializer.parse(i, itemDefinition, itemDeserializer)))
             .filter(Optional::isPresent)
             .map(Optional::get)
             .forEach(sequenceOf::add);
        return sequenceOf;
    }

}
